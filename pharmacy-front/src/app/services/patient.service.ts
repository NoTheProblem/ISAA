import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {MedicineModel} from '../model/medicine.model';
import {ToastrService} from 'ngx-toastr';
import {Observable} from 'rxjs';
import {PatientModel} from '../model/patient.model';
import {Constants} from './constants';
import {UserModel} from '../model/user.model';
import {LoyaltyProgram} from '../model/loyalty.model';
import {ExaminationModel} from '../model/examination.model';
import {PharmacyModel} from '../model/pharmacy.model';
import {CounselingModel} from '../model/counseling.model';
import {ReservationModel} from '../model/reservation.model';

@Injectable()
export class PatientService {

  constructor(
    private httpClient: HttpClient,
    private toast: ToastrService
  ) {
  }

  public addAllergy(medicine: MedicineModel): void {
    this.httpClient.post(Constants.API + '/patient/addAllergy', medicine).subscribe(
      (response: any) => {
        this.toast.success(`Dodata alergija na ${medicine.name}`);
      },
      (error => {
        this.toast.error(`Nije dodata alergija na ${medicine.name}`);
      })
    );
  }

  public addExamination(examination: ExaminationModel): void {
    this.httpClient.post(Constants.API + '/patient/addExamination', examination).subscribe(
      (response: any) => {
        this.toast.success(`Termin je zakazan za ${examination.date}`);
      },
      (error => {
        this.toast.error(`Termin nije zakazan za ${examination.date}`);
      })
    );
  }

  public getAllAllergyForPatient(username: string): Observable<Array<MedicineModel>> {
    console.log('dsadsa');
    return this.httpClient.get<Array<MedicineModel>>(Constants.API + '/patient/getAllAllergyForPatient/' + username);
  }



  public getAll(): Observable<Array<PatientModel>> {
    return this.httpClient.get<Array<PatientModel>>(Constants.API + '/patient/getAll');
  }



  public getPatientLoyalty(username: string): Observable<LoyaltyProgram> {
    console.log('dsadsa');
    return this.httpClient.get<LoyaltyProgram>(Constants.API + '/patient/getLoyaltyProgram/' + username);
  }

  public cancelExamination(examination: ExaminationModel): void {
    this.httpClient.post(Constants.API + '/patient/cancelExamination', examination).subscribe(
      (response: true) => {
        this.toast.success(`Termin je otkazan za ${examination.date}`);
      },
      (error => {
        this.toast.error(`Termin nije otkazan za ${examination.date}`);
      }),
    );
  }

  public scheduledCounseling(counseling: CounselingModel): void {
    this.httpClient.post(Constants.API + '/patient/addCounseling', counseling).subscribe(
      (response: any) => {
        this.toast.success(`Termin je zakazan za ${counseling.date}`);
      },
      (error => {
        this.toast.error(`Termin nije zakazan za ${counseling.date}`);
      })
    );

  }

  public cancelCounseling(counseling: CounselingModel): void {
    this.httpClient.post(Constants.API + '/patient/cancelCounseling', counseling).subscribe(
      (response: true) => {
        this.toast.success(`Termin je otkazan za ${counseling.date}`);
      },
      (error => {
        this.toast.error(`Termin nije otkazan za ${counseling.date}`);
      }),
    );
  }
  public getAllPharmacyForMedicine(medicine: MedicineModel): Observable<Array<PharmacyModel>> {
    return this.httpClient.get<Array<PharmacyModel>>(Constants.API + '/patient/getAllPharmacyForMedicine');
  }

  public reserveMedicine(reservation: ReservationModel): void {
    console.log(reservation);
    this.httpClient.post(Constants.API + '/patient/reserveMedicine', reservation).subscribe(
      (response: true) => {
        this.toast.success(`Rezervisan je lek `);
      },
      (error => {
        this.toast.error(`Rezervisan je lek `);
      }),
    );
  }

  public getAllReservedMedication(): Observable<Array<ReservationModel>> {
    return this.httpClient.get<Array<ReservationModel>>(Constants.API + '/patient/getAllReservedMedicine');
  }

  public cancelReservation(reservation: ReservationModel): void {
    this.httpClient.post(Constants.API + '/patient/cancelReservation', reservation).subscribe(
      (response: true) => {
        this.toast.success(`Rezervacija je otkazana za ${reservation.id}`);
      },
      (error => {
        this.toast.error(`Termin nije otkazan za ${reservation.id}`);
      }),
    );
  }





}
