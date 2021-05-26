import { Component, OnInit } from '@angular/core';
import {OrderFormModel} from '../model/order-form.model';
import {OrderFormService} from '../services/order-form.service';
import {MedicineModel} from '../model/medicine.model';
import {MedicineService} from '../services/medicine.service';
import {MedicineQuantityModel} from '../model/medicineQuantity.model';
import {MedicineQuantityHelpModel} from '../model/medicineQuantityHelpModel';

@Component({
  selector: 'app-order-froms',
  templateUrl: './order-froms.component.html',
  styleUrls: ['./order-froms.component.css']
})
export class OrderFromsComponent implements OnInit {
  public orders: Array<OrderFormModel>;
  public medicines: Array<MedicineModel>;
  public order: OrderFormModel;
  public medQuantity: MedicineQuantityModel;
  public medHelp: Array<MedicineQuantityHelpModel> = new Array<MedicineQuantityHelpModel>();
  public medH: MedicineQuantityHelpModel;
  public ids: Array<number>;
  public quan: Array<number>;
  public mapa: Map<number, number>;

  helpID: number;
  isQuanShown = false;
  isShown = true ;
  isSuccessful = false;
  form: any = {};
  isSignUpFailed = false;
  errorMessage = '';
  name = '';
  dummy = '';
  newquan: number;

  constructor(
    private medicineService: MedicineService,
    private orderFormService: OrderFormService
  ) {
  }

  ngOnInit(): void {
    this.initMedicines();
    this.mapa = new Map<number, number>();
  }

  public toggle(): void {
    this.isShown = ! this.isShown;
  }

  public search(): void {
    if (this.name === '') {
      this.medicines = null;
    } else {
      this.medicines = this.medicines.filter(res => {
        return res.name.toLocaleLowerCase().match(this.name.toLocaleLowerCase()) ||
        res.code.toLocaleLowerCase().match(this.name.toLocaleLowerCase());
      });

    }
  }

  private initMedicines(): void {
    this.orderFormService.getAllActive()
      .subscribe((orders: Array<OrderFormModel>) => {
        this.orders = orders;
      });
    this.medicineService.getAll()
      .subscribe((medicineList: Array<MedicineModel>) => {
        this.medicines = medicineList;
      });
  }


  public addToOrderForm(medicine: MedicineModel): void {
    // @ts-ignore
    this.medicines.pop(medicine);
    this.medH = new MedicineQuantityHelpModel(medicine.id, 1, medicine.code, medicine.name);
    this.medHelp.push(this.medH);
  }

  public Izbrisi(med: MedicineQuantityHelpModel): void {
    // @ts-ignore
    this.medHelp.pop(med);
    this.medicines.push(new MedicineModel(med.medicineID, med.medicineName, med.medicineCode, '', '', '', '', '', '', '', ''));
  }


  public createOrder(): void {
    this.ids = new Array<number>();
    this.quan = new Array<number>();
    for (const medHelpItem of this.medHelp){
      this.ids.push(medHelpItem.medicineID);
      this.quan.push(medHelpItem.quantity);
    }
    this.medQuantity = new MedicineQuantityModel(1, this.ids, this.quan);
    this.order = new OrderFormModel(null,  null, null, null, null, this.form.startDate, null, this.medQuantity);
    this.orderFormService.addOrderForm(this.order);
    window.location.reload();
  }

  onSubmit(): void {}

  Dodaj(med: MedicineQuantityHelpModel): void {
      med.quantity = med.quantity + 1;
  }

  Smanji(med: MedicineQuantityHelpModel): void {
    if ( med.quantity - 1 < 1){
      return;
    }
    med.quantity = med.quantity - 1;
  }

  ShowQuanEnter(med: MedicineQuantityHelpModel): void {
    this.isQuanShown = true;
    this.medH = med;
    this.newquan = med.quantity;
  }

  changeQuan(): void {
    this.medH.quantity = this.newquan;
    this.isQuanShown = false;
  }

}

