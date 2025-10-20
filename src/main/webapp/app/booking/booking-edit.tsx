import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate, useParams } from 'react-router';
import { handleServerError, setYupDefaults } from 'app/common/utils';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { BookingDTO } from 'app/booking/booking-model';
import axios from 'axios';
import InputRow from 'app/common/input-row/input-row';
import useDocumentTitle from 'app/common/use-document-title';
import * as yup from 'yup';


function getSchema() {
  setYupDefaults();
  return yup.object({
    checkinDate: yup.string().emptyToNull().required(),
    checkoutDate: yup.string().emptyToNull().required(),
    status: yup.string().emptyToNull(),
    user: yup.number().integer().emptyToNull().required(),
    room: yup.string().emptyToNull().uuid().required()
  });
}

export default function BookingEdit() {
  const { t } = useTranslation();
  useDocumentTitle(t('booking.edit.headline'));

  const navigate = useNavigate();
  const [userValues, setUserValues] = useState<Map<number,string>>(new Map());
  const [roomValues, setRoomValues] = useState<Record<string,string>>({});
  const params = useParams();
  const currentId = params.id!;

  const useFormResult = useForm({
    resolver: yupResolver(getSchema()),
  });

  const prepareForm = async () => {
    try {
      const userValuesResponse = await axios.get('/api/v1/bookings/userValues');
      setUserValues(userValuesResponse.data);
      const roomValuesResponse = await axios.get('/api/v1/bookings/roomValues');
      setRoomValues(roomValuesResponse.data);
      const data = (await axios.get('/api/v1/bookings/' + currentId)).data;
      useFormResult.reset(data);
    } catch (error: any) {
      handleServerError(error, navigate);
    }
  };

  useEffect(() => {
    prepareForm();
  }, []);

  const updateBooking = async (data: BookingDTO) => {
    window.scrollTo(0, 0);
    try {
      await axios.put('/api/v1/bookings/' + currentId, data);
      navigate('/bookings', {
            state: {
              msgSuccess: t('booking.update.success')
            }
          });
    } catch (error: any) {
      handleServerError(error, navigate, useFormResult.setError, t);
    }
  };

  return (<>
    <div className="flex flex-wrap mb-6">
      <h1 className="grow text-3xl md:text-4xl font-medium mb-2">{t('booking.edit.headline')}</h1>
      <div>
        <Link to="/bookings" className="inline-block text-white bg-gray-500 hover:bg-gray-600 focus:ring-gray-200 focus:ring-4 rounded px-5 py-2">{t('booking.edit.back')}</Link>
      </div>
    </div>
    <form onSubmit={useFormResult.handleSubmit(updateBooking)} noValidate>
      <InputRow useFormResult={useFormResult} object="booking" field="id" disabled={true} />
      <InputRow useFormResult={useFormResult} object="booking" field="checkinDate" required={true} type="datepicker" />
      <InputRow useFormResult={useFormResult} object="booking" field="checkoutDate" required={true} type="datepicker" />
      <InputRow useFormResult={useFormResult} object="booking" field="status" required={true} type="radio" options={{'ACTIVE': 'ACTIVE', 'CANCELLED': 'CANCELLED'}} />
      <InputRow useFormResult={useFormResult} object="booking" field="user" required={true} type="select" options={userValues} />
      <InputRow useFormResult={useFormResult} object="booking" field="room" required={true} type="select" options={roomValues} />
      <input type="submit" value={t('booking.edit.headline')} className="inline-block text-white bg-blue-600 hover:bg-blue-700 focus:ring-blue-300  focus:ring-4 rounded px-5 py-2 cursor-pointer mt-6" />
    </form>
  </>);
}
