import { Component } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../_services/user.service';
import { take } from 'rxjs';

@Component({
  selector: 'app-callback',
  imports: [MatProgressSpinnerModule],
  templateUrl: './callback.component.html',
  styleUrl: './callback.component.css'
})
export class CallbackComponent {
  constructor(
    private route: ActivatedRoute,
    private authService: UserService,
    private router: Router
  ) {}

  ngOnInit() {
  this.route.queryParams.pipe(
    take(1) // Ensure single execution
  ).subscribe(params => {
    const code = params['code'];
    if (!code) {
      this.router.navigate(['/login'], { 
        queryParams: { error: 'missing_code' }
      });
      return;
    }
    
    this.authService.handleGoogleCallback(code);
  });
}

}
