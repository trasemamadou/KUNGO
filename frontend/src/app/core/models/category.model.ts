export interface CategorySummary {
  id: string;
  name: string;
  slug: string;
  icon?: string;
}

export interface Category {
  id: string;
  name: string;
  slug: string;
  description?: string;
  icon?: string;
  image?: string;
  parentId?: string;
  parentName?: string;
  active: boolean;
  displayOrder: number;
  children: CategorySummary[];
}
