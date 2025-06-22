export interface UserImage {
  id: number;
  fileName: string;
  downloadUrl: string;
}

export interface UserProfile {
  id: number;
  firstName: string;
  lastName: string;
  userImage: UserImage | null;
}

