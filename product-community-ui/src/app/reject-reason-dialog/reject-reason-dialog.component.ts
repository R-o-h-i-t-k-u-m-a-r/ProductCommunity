import { Component } from '@angular/core';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-reject-reason-dialog',
   imports: [MatInputModule, MatButtonModule, FormsModule, MatDialogModule],
  templateUrl: './reject-reason-dialog.component.html',
  styleUrl: './reject-reason-dialog.component.css'
})
export class RejectReasonDialogComponent {
  reason = '';

  constructor(public dialogRef: MatDialogRef<RejectReasonDialogComponent>) {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onReject(): void {
    this.dialogRef.close(this.reason);
  }
}
