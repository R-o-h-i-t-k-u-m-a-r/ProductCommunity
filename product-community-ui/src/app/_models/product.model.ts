export interface ApiResponse<T> {
  data: T;
  message?: string;
  success?: boolean;
}

export interface CreateReviewRequest {
  productName: string;
  productCode: string;
  productBrand: string;
}

export interface ReviewRequestDTO {
  id: number;
  productName: string;
  productCode: string;
  productBrand: string;
  status: string;
  createdAt: string;
  updatedAt?: string;
  userDTO: UserDTO;
}

export interface Page<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      unsorted: boolean;
      sorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalElements: number;
  totalPages: number;
  first: boolean;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    unsorted: boolean;
    sorted: boolean;
  };
  numberOfElements: number;
  empty: boolean;
}

export interface ReviewResponse {
  id: number;
  productId: number;
  productName: string;
  userId: number;
  userName: string;
  userAvatar: string | null;
  rating: number;
  title: string;
  content: string;
  status: ReviewStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ProductInfoDto {
  id: number;
  name: string;
  brand: string;
  code: string;
  description: string;
  avgRating: number;
  totalReveiws: number;
  images: ProductImageDTO[];
}

export interface ProductDTO {
  id: number;
  name: string;
  brand: string;
  code: string;
  description: string;
  images: ProductImageDTO[];
  reviews: ReviewDTO[];
  averageRating?: number;
  reviewCount?: number;
}

export interface ProductImageDTO {
  id: number;
  fileName: string;
  downloadUrl: string;
}

export interface ReviewDTO {
  id: number;
  rating: number;
  title: string;
  content: string;
  reviewStatus: ReviewStatus;
  createdAt: string;
  updatedAt: string;
  userDTO: UserDTO;
}

export interface UserDTO {
  id: number;
  firstName: string;
  lastName: string;
  userName: string;
  roles: Role[];
  userImage: UserImageDto;
}

export interface UserImageDto {
  id: number;
  fileName: string;
  downloadUrl: string;
}

export interface ProductReviewRequest {
  productId: number;
  rating: number;
  title: string;
  content: string;
}

export enum ReviewStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

export interface Role {
  id: number;
  name: string;
}
