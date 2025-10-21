import React from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import App from "./app";
import Home from './home/home';
import BookingList from './booking/booking-list';
import BookingAdd from './booking/booking-add';
import BookingEdit from './booking/booking-edit';
import RoomList from './room/room-list';
import RoomAdd from './room/room-add';
import RoomEdit from './room/room-edit';
import Error from './error/error';
import { ADMIN, USER } from 'app/security/authentication-provider';


export default function AppRoutes() {
  const router = createBrowserRouter([
    {
      element: <App />,
      children: [
        { path: '', element: <Home /> , handle: { roles: [ADMIN] } },
        { path: 'bookings', element: <BookingList /> , handle: { roles: [ADMIN, USER] } },
        { path: 'bookings/add', element: <BookingAdd /> , handle: { roles: [ADMIN] } },
        { path: 'bookings/edit/:id', element: <BookingEdit /> , handle: { roles: [ADMIN] } },
        { path: 'rooms', element: <RoomList /> , handle: { roles: [USER] } },
        { path: 'rooms/add', element: <RoomAdd /> },
        { path: 'rooms/edit/:id', element: <RoomEdit /> },
        { path: 'error', element: <Error /> },
        { path: '*', element: <Error /> }
      ]
    }
  ]);

  return (
    <RouterProvider router={router} />
  );
}
