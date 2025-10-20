import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate, useSearchParams } from 'react-router';
import { handleServerError, getListParams } from 'app/common/utils';
import { RoomDTO } from 'app/room/room-model';
import { Pagination } from 'app/common/list-helper/pagination';
import { ListModel } from 'app/common/hateoas';
import axios from 'axios';
import SearchFilter from 'app/common/list-helper/search-filter';
import Sorting from 'app/common/list-helper/sorting';
import useDocumentTitle from 'app/common/use-document-title';


export default function RoomList() {
  const { t } = useTranslation();
  useDocumentTitle(t('room.list.headline'));

  const [rooms, setRooms] = useState<ListModel<RoomDTO>|undefined>(undefined);
  const navigate = useNavigate();
  const [searchParams, ] = useSearchParams();
  const listParams = getListParams();
  const sortOptions = {
    'id,ASC': t('room.list.sort.id,ASC'), 
    'roomNumber,ASC': t('room.list.sort.roomNumber,ASC'), 
    'roomType,ASC': t('room.list.sort.roomType,ASC')
  };

  const getAllRooms = async () => {
    try {
      const response = await axios.get('/api/v1/rooms?' + listParams);
      setRooms(response.data);
    } catch (error: any) {
      handleServerError(error, navigate);
    }
  };

  const confirmDelete = async (id: string) => {
    if (!confirm(t('delete.confirm'))) {
      return;
    }
    try {
      await axios.delete('/api/v1/rooms/' + id);
      navigate('/rooms', {
            state: {
              msgInfo: t('room.delete.success')
            }
          });
      getAllRooms();
    } catch (error: any) {
      if (error?.response?.data?.code === 'REFERENCED') {
        const messageParts = error.response.data.message.split(',');
        navigate('/rooms', {
              state: {
                msgError: t(messageParts[0]!, { id: messageParts[1]! })
              }
            });
        return;
      }
      handleServerError(error, navigate);
    }
  };

  useEffect(() => {
    getAllRooms();
  }, [searchParams]);

  return (<>
    <div className="flex flex-wrap mb-6">
      <h1 className="grow text-3xl md:text-4xl font-medium mb-2">{t('room.list.headline')}</h1>
      <div>
        <Link to="/rooms/add" className="inline-block text-white bg-blue-600 hover:bg-blue-700 focus:ring-blue-300  focus:ring-4 rounded px-5 py-2">{t('room.list.createNew')}</Link>
      </div>
    </div>
    {((rooms?._embedded && rooms?.page?.totalElements !== 0) || searchParams.get('filter')) && (
    <div className="flex flex-wrap justify-between">
      <SearchFilter placeholder={t('room.list.filter')} />
      <Sorting sortOptions={sortOptions} />
    </div>
    )}
    {!rooms?._embedded || rooms?.page?.totalElements === 0 ? (
    <div>{t('room.list.empty')}</div>
    ) : (<>
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead>
          <tr>
            <th scope="col" className="text-left p-2">{t('room.id.label')}</th>
            <th scope="col" className="text-left p-2">{t('room.roomNumber.label')}</th>
            <th scope="col" className="text-left p-2">{t('room.roomType.label')}</th>
            <th scope="col" className="text-left p-2">{t('room.capacity.label')}</th>
            <th scope="col" className="text-left p-2">{t('room.price.label')}</th>
            <th scope="col" className="text-left p-2">{t('room.amenities.label')}</th>
            <th scope="col" className="text-left p-2">{t('room.active.label')}</th>
            <th></th>
          </tr>
        </thead>
        <tbody className="border-t-2 border-black">
          {rooms?._embedded?.['roomDTOList']?.map((room) => (
          <tr key={room.id} className="odd:bg-gray-100">
            <td className="p-2">{room.id}</td>
            <td className="p-2">{room.roomNumber}</td>
            <td className="p-2">{room.roomType}</td>
            <td className="p-2">{room.capacity}</td>
            <td className="p-2">{room.price}</td>
            <td className="p-2">{room.amenities}</td>
            <td className="p-2">{room.active?.toString()}</td>
            <td className="p-2">
              <div className="float-right whitespace-nowrap">
                <Link to={'/rooms/edit/' + room.id} className="inline-block text-white bg-gray-500 hover:bg-gray-600 focus:ring-gray-200 focus:ring-3 rounded px-2.5 py-1.5 text-sm">{t('room.list.edit')}</Link>
                <span> </span>
                <button type="button" onClick={() => confirmDelete(room.id!)} className="inline-block text-white bg-gray-500 hover:bg-gray-600 focus:ring-gray-200 focus:ring-3 rounded px-2.5 py-1.5 text-sm cursor-pointer">{t('room.list.delete')}</button>
              </div>
            </td>
          </tr>
          ))}
        </tbody>
      </table>
    </div>
    <Pagination page={rooms?.page} />
    </>)}
  </>);
}
