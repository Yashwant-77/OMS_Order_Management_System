import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from "@angular/common/http";
import { UserService } from "../services/user/user.service";
import { Router } from "@angular/router";
// import { Observable, throwError } from "rxjs";
import { catchError } from "rxjs/operators";

export class AuthInterceptor implements HttpInterceptor {
    constructor(private userService:UserService , private router : Router){}
    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        if(req.headers.get("No-Auth") === 'True'){
            return next.handle(req.clone());
        }

        const token = this.userService.getToken();
        this.addToken(req , token);

        return next.handle(req).pipe(
            catchError(
                (err : HttpErrorResponse)=>{
                    console.log(err.status);
                    if(err.status === 401){
                        this.router.navigate(['/login'])
                    }
                    else if(err.status === 403){
                        this.router.navigate(['/forbidden']);
                    }
                    return throwError("Some thing is wrong");
                }
            )
        )
    }

    private addToken(request:HttpRequest<any> , token : string)
    {
        return request.clone(
            {
                setHeaders : {
                    Authorization : `Bearer ${token}`
                }
            }
        )
    }

    
}