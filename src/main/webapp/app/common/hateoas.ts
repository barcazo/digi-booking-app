import { Page } from 'app/common/list-helper/pagination';


export class Resource {

  _links?: Record<string, Link>;

}

export interface Link {

  href: string;
  hreflang: string;
  title: string;
  type: string;
  deprecation: string;
  profile: string;
  name: string;
  templated: boolean;

}

export class ListModel<ResourceType extends Resource> extends Resource {

  _embedded?: Record<string, ResourceType[]>;
  page?: Page;

}
