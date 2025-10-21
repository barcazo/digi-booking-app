import React, { createContext, useEffect, useState, ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { useMatches, useNavigate } from 'react-router';
import Keycloak from 'keycloak-js';
import axios from 'axios';


export const ADMIN = 'ADMIN';
export const USER = 'USER';

export const AuthenticationContext = createContext<{
  isLoggedIn: () => boolean;
  getToken: () => Promise<string|null>;
  login: (targetUrl?: string) => Promise<void>;
  logout: () => Promise<void>;
}>({
  isLoggedIn: () => false,
  getToken: async () => null,
  login: async () => {},
  logout: async () => {}
});

/**
 * Central management of authentication. Checks the availability of a required role before loading a route.
 * Initializes the Keycloak connection.
 */
export const AuthenticationProvider = ({ children }: AuthenticationProviderParams) => {
  const { t } = useTranslation();
  const [initCompleted, setInitCompleted] = useState(0);
  const [keycloak, setKeycloak] = useState<Keycloak|null>(null);
  const navigate = useNavigate();
  const matches = useMatches();
  const roles = matches.reduce((accumulator: string[], currentMatch) => {
    return accumulator.concat((currentMatch.handle as RolesHandle)?.roles || []);
  }, []);

  const initKeycloak = async () => {
    const instance = new Keycloak({
          url: process.env.KEYCLOAK_URL || 'http://localhost:8085',
          realm: process.env.KEYCLOAK_REALM || 'digi-id',
          clientId: process.env.KEYCLOAK_CLIENT_ID || 'digi-id'
        });
    console.log('Keycloak URL:', process.env.KEYCLOAK_URL);
    await instance.init({
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: location.origin + '/silent-check-sso.html'
    });
    setKeycloak(instance);
  };

  const getToken = async () => {
    if (!isLoggedIn()) {
      return null;
    }
    if (keycloak?.isTokenExpired()) {
      // update token before continuing
      try {
        await keycloak.updateToken();
      } catch (error) {
        return null;
      }
    }
    return keycloak?.token || null;
  };

  const isLoggedIn = () => {
    return keycloak?.authenticated || false;
  };

  const login = async (targetUrl?: string) => {
    await keycloak?.login({
      redirectUri: location.origin + (targetUrl || '/')
    });
  };

  const logout = async () => {
    await keycloak?.logout({
      redirectUri: location.origin
    });
  };

  const hasAnyRole = () => {
    const currentRoles = keycloak!!.realmAccess!!['roles'];
    return roles.some((requiredRole) => currentRoles.includes(requiredRole));
  };

  useEffect(() => {
    (async () => {
      await initKeycloak();
      setInitCompleted(initCompleted + 1);
    })();
  }, []);

  useEffect(() => {
    // include token in outgoing requests
    let interceptor = axios.interceptors.request.use(
        async (config) => {
          const token = await getToken();
          if (token) {
            config.headers['Authorization'] = 'Bearer ' + token;
          }
          return config;
        },
        (error) => {
          return Promise.reject(error);
        });
    setInitCompleted(initCompleted + 1);
    return () => axios.interceptors.request.eject(interceptor);
  }, [keycloak]);

  const checkAccessAllowed = () => {
    if (roles.length > 0 && !isLoggedIn()) {
      return 'login-required';
    } else if (roles.length > 0 && !hasAnyRole()) {
      return 'missing-role';
    }
    return null;
  };

  useEffect(() => {
    const accessError = checkAccessAllowed();
    if (accessError === 'login-required') {
      // show login page
      login(location.pathname);
    } else if (accessError === 'missing-role') {
      // show error page with message
      navigate('/error', {
            state: {
              errorStatus: '403',
              msgError: t('authentication.role.missing')
            }
          });
    }
  }, [matches, isLoggedIn]);

  if (checkAccessAllowed() !== null) {
    // don't render current route
    return;
  }
  return <AuthenticationContext.Provider value={{ isLoggedIn, getToken, login, logout }}>{initCompleted >= 2 && children}</AuthenticationContext.Provider>;
};

interface AuthenticationProviderParams {
  children: ReactNode;
}

interface RolesHandle {
  roles?: string[];
}
