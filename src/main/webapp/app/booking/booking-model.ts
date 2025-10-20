import { Resource } from 'app/common/hateoas';


export class BookingDTO extends Resource  {

  constructor(data:Partial<BookingDTO>) {
    super();
    Object.assign(this, data);
  }

  id?: string|null;
  checkinDate?: string|null;
  checkoutDate?: string|null;
  status?: string|null;
  user?: number|null;
  room?: string|null;

}
