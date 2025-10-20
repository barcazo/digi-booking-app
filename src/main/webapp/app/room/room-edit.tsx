import React, { useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate, useParams } from 'react-router';
import { handleServerError, setYupDefaults } from 'app/common/utils';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { RoomDTO } from 'app/room/room-model';
import axios from 'axios';
import InputRow from 'app/common/input-row/input-row';
import useDocumentTitle from 'app/common/use-document-title';
import * as yup from 'yup';


function getSchema() {
  setYupDefaults();
  return yup.object({
    roomNumber: yup.number().integer().emptyToNull().required(),
    roomType: yup.string().emptyToNull().max(255).required(),
    capacity: yup.number().integer().emptyToNull().required(),
    price: yup.string().emptyToNull().numeric(10, 2).required(),
    amenities: yup.string().emptyToNull().max(255).required(),
    active: yup.bool()
  });
}

export default function RoomEdit() {
  const { t } = useTranslation();
  useDocumentTitle(t('room.edit.headline'));

  const navigate = useNavigate();
  const params = useParams();
  const currentId = params.id!;

  const useFormResult = useForm({
    resolver: yupResolver(getSchema()),
  });

  const prepareForm = async () => {
    try {
      const data = (await axios.get('/api/v1/rooms/' + currentId)).data;
      useFormResult.reset(data);
    } catch (error: any) {
      handleServerError(error, navigate);
    }
  };

  useEffect(() => {
    prepareForm();
  }, []);

  const updateRoom = async (data: RoomDTO) => {
    window.scrollTo(0, 0);
    try {
      await axios.put('/api/v1/rooms/' + currentId, data);
      navigate('/rooms', {
            state: {
              msgSuccess: t('room.update.success')
            }
          });
    } catch (error: any) {
      handleServerError(error, navigate, useFormResult.setError, t);
    }
  };

  return (<>
    <div className="flex flex-wrap mb-6">
      <h1 className="grow text-3xl md:text-4xl font-medium mb-2">{t('room.edit.headline')}</h1>
      <div>
        <Link to="/rooms" className="inline-block text-white bg-gray-500 hover:bg-gray-600 focus:ring-gray-200 focus:ring-4 rounded px-5 py-2">{t('room.edit.back')}</Link>
      </div>
    </div>
    <form onSubmit={useFormResult.handleSubmit(updateRoom)} noValidate>
      <InputRow useFormResult={useFormResult} object="room" field="id" disabled={true} />
      <InputRow useFormResult={useFormResult} object="room" field="roomNumber" required={true} type="number" />
      <InputRow useFormResult={useFormResult} object="room" field="roomType" required={true} />
      <InputRow useFormResult={useFormResult} object="room" field="capacity" required={true} type="number" />
      <InputRow useFormResult={useFormResult} object="room" field="price" required={true} />
      <InputRow useFormResult={useFormResult} object="room" field="amenities" required={true} />
      <InputRow useFormResult={useFormResult} object="room" field="active" type="checkbox" />
      <input type="submit" value={t('room.edit.headline')} className="inline-block text-white bg-blue-600 hover:bg-blue-700 focus:ring-blue-300  focus:ring-4 rounded px-5 py-2 cursor-pointer mt-6" />
    </form>
  </>);
}
