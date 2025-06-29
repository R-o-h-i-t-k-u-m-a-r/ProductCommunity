import { Component } from '@angular/core';
import { ProductListComponent } from "../product-list/product-list.component";

@Component({
  selector: 'app-user',
  imports: [ ProductListComponent],
  templateUrl: './user.component.html',
  styleUrl: './user.component.css'
})
export class UserComponent {

}
