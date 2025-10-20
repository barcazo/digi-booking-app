import { Resource } from 'app/common/hateoas';


export class RoomDTO extends Resource  {

  constructor(data:Partial<RoomDTO>) {
    super();
    Object.assign(this, data);
  }

  id?: string|null;
  roomNumber?: number|null;
  roomType?: string|null;
  capacity?: number|null;
  price?: string|null;
  amenities?: string|null;
  active?: boolean|null;

}
