import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate, useSearchParams } from 'react-router';
import { handleServerError, getListParams } from 'app/common/utils';
import { BookingDTO } from 'app/booking/booking-model';
import { Pagination } from 'app/common/list-helper/pagination';
import { ListModel } from 'app/common/hateoas';
import axios from 'axios';
import SearchFilter from 'app/common/list-helper/search-filter';
import Sorting from 'app/common/list-helper/sorting';
import useDocumentTitle from 'app/common/use-document-title';


export default function BookingList() {
  const { t } = useTranslation();
  useDocumentTitle(t('booking.list.headline'));

  const [bookings, setBookings] = useState<ListModel<BookingDTO>|undefined>(undefined);
  const navigate = useNavigate();
  const [searchParams, ] = useSearchParams();
  const listParams = getListParams();
  const sortOptions = {
    'id,ASC': t('booking.list.sort.id,ASC'), 
    'checkinDate,ASC': t('booking.list.sort.checkinDate,ASC'), 
    'checkoutDate,ASC': t('booking.list.sort.checkoutDate,ASC')
  };

  const getAllBookings = async () => {
    try {
      const response = await axios.get('/api/v1/bookings?' + listParams);
      setBookings(response.data);
    } catch (error: any) {
      handleServerError(error, navigate);
    }
  };

  const confirmDelete = async (id: string) => {
    if (!confirm(t('delete.confirm'))) {
      return;
    }
    try {
      await axios.delete('/api/v1/bookings/' + id);
      navigate('/bookings', {
            state: {
              msgInfo: t('booking.delete.success')
            }
          });
      getAllBookings();
    } catch (error: any) {
      handleServerError(error, navigate);
    }
  };

  useEffect(() => {
    getAllBookings();
  }, [searchParams]);

  return (<>
    <div className="flex flex-wrap mb-6">
      <h1 className="grow text-3xl md:text-4xl font-medium mb-2">{t('booking.list.headline')}</h1>
      <div>
        <Link to="/bookings/add" className="inline-block text-white bg-blue-600 hover:bg-blue-700 focus:ring-blue-300  focus:ring-4 rounded px-5 py-2">{t('booking.list.createNew')}</Link>
      </div>
    </div>
    {((bookings?._embedded && bookings?.page?.totalElements !== 0) || searchParams.get('filter')) && (
    <div className="flex flex-wrap justify-between">
      <SearchFilter placeholder={t('booking.list.filter')} />
      <Sorting sortOptions={sortOptions} />
    </div>
    )}
    {!bookings?._embedded || bookings?.page?.totalElements === 0 ? (
    <div>{t('booking.list.empty')}</div>
    ) : (<>
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead>
          <tr>
            <th scope="col" className="text-left p-2">{t('booking.id.label')}</th>
            <th scope="col" className="text-left p-2">{t('booking.checkinDate.label')}</th>
            <th scope="col" className="text-left p-2">{t('booking.checkoutDate.label')}</th>
            <th scope="col" className="text-left p-2">{t('booking.status.label')}</th>
            <th scope="col" className="text-left p-2">{t('booking.user.label')}</th>
            <th scope="col" className="text-left p-2">{t('booking.room.label')}</th>
            <th></th>
          </tr>
        </thead>
        <tbody className="border-t-2 border-black">
          {bookings?._embedded?.['bookingDTOList']?.map((booking) => (
          <tr key={booking.id} className="odd:bg-gray-100">
            <td className="p-2">{booking.id}</td>
            <td className="p-2">{booking.checkinDate}</td>
            <td className="p-2">{booking.checkoutDate}</td>
            <td className="p-2">{booking.status}</td>
            <td className="p-2">{booking.user}</td>
            <td className="p-2">{booking.room}</td>
            <td className="p-2">
              <div className="float-right whitespace-nowrap">
                <Link to={'/bookings/edit/' + booking.id} className="inline-block text-white bg-gray-500 hover:bg-gray-600 focus:ring-gray-200 focus:ring-3 rounded px-2.5 py-1.5 text-sm">{t('booking.list.edit')}</Link>
                <span> </span>
                <button type="button" onClick={() => confirmDelete(booking.id!)} className="inline-block text-white bg-gray-500 hover:bg-gray-600 focus:ring-gray-200 focus:ring-3 rounded px-2.5 py-1.5 text-sm cursor-pointer">{t('booking.list.delete')}</button>
              </div>
            </td>
          </tr>
          ))}
        </tbody>
      </table>
    </div>
    <Pagination page={bookings?.page} />
    </>)}
  </>);
}
