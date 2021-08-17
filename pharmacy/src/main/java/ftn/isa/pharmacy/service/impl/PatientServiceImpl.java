package ftn.isa.pharmacy.service.impl;

import ftn.isa.pharmacy.config.MailConfig;
import ftn.isa.pharmacy.dto.CounselingDTO;
import ftn.isa.pharmacy.dto.ExaminationDto;
import ftn.isa.pharmacy.dto.MedicineDto;
import ftn.isa.pharmacy.dto.ReservationDTO;
import ftn.isa.pharmacy.exception.ResourceConflictException;
import ftn.isa.pharmacy.mapper.CounselingMapper;
import ftn.isa.pharmacy.mapper.ExaminationMapper;
import ftn.isa.pharmacy.mapper.MedicineMapper;
import ftn.isa.pharmacy.mapper.ReservationMapper;
import ftn.isa.pharmacy.model.*;
import ftn.isa.pharmacy.repository.*;
import ftn.isa.pharmacy.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PatientServiceImpl implements PatientService {

    final private MailConfig mailConfig;
    private final MedicineMapper medicineMapper;
    private final ExaminationMapper examinationMapper;
    private final PatientRepository patientRepository;
    private final ExaminationRepository examinationRepository;
    private final LoyaltyProgramRepository loyaltyProgramRepository;
    private final CounselingMapper counselingMapper;
    private final CounselingRepository counselingRepository;
    private final PharmacistRepository pharmacistRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final PharmacyRepository pharmacyRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineQuantityPharmacyRepository medicineQuantityPharmacyRepository;

    @Autowired
    public PatientServiceImpl(MedicineQuantityPharmacyRepository medicineQuantityPharmacyRepository, MedicineRepository medicineRepository, PharmacyRepository pharmacyRepository, ReservationMapper reservationMapper, ReservationRepository reservationRepository,PharmacistRepository pharmacistRepository, CounselingRepository counselingRepository, CounselingMapper counselingMapper, MailConfig mailConfig, MedicineMapper medicineMapper, PatientRepository patientRepository, LoyaltyProgramRepository loyaltyProgramRepository, ExaminationMapper examinationMapper, ExaminationRepository examinationRepository) {
        this.medicineMapper = medicineMapper;
        this.patientRepository = patientRepository;
        this.loyaltyProgramRepository = loyaltyProgramRepository;
        this.examinationMapper = examinationMapper;
        this.examinationRepository = examinationRepository;
        this.mailConfig = mailConfig;
        this.counselingMapper = counselingMapper;
        this.counselingRepository = counselingRepository;
        this.pharmacistRepository = pharmacistRepository;
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.pharmacyRepository = pharmacyRepository;
        this.medicineRepository = medicineRepository;
        this.medicineQuantityPharmacyRepository = medicineQuantityPharmacyRepository;
    }

    private JavaMailSenderImpl getJMS(){
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(this.mailConfig.getHost());
        mailSender.setPort(this.mailConfig.getPort());
        mailSender.setUsername(this.mailConfig.getUsername());
        mailSender.setPassword(this.mailConfig.getPassword());
        return mailSender;
    }

    @Override
    public void addAllergy(MedicineDto medicineDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Patient patient = (Patient) authentication.getPrincipal(); --> ovo radi, ali ne povuce allergicMedicines jer je lazy
        Optional<Patient> patientOptional = patientRepository.findById(((User) authentication.getPrincipal()).getId());
        if(patientOptional.isPresent()) {
            Medicine medicine = medicineMapper.bean2Entity(medicineDto);
            Patient patient = patientOptional.get();
            patient.getAllergicMedicines().add(medicine);
            patientRepository.saveAndFlush(patient);
        }
    }

    @Override
    public void addExamination(ExaminationDto examinationDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Patient patient = (Patient) authentication.getPrincipal(); --> ovo radi, ali ne povuce allergicMedicines jer je lazy
        Optional<Patient> patientOptional = patientRepository.findById(((User) authentication.getPrincipal()).getId());
        if(patientOptional.isPresent()) {
            Examination examination = examinationMapper.bean2Entity(examinationDto);
            Optional<Examination> examination1 = examinationRepository.findById(examinationDto.getId());
            examination = examination1.get();

            Patient patient = patientOptional.get();
            examination.setPatient(patient);
            examination.setFree(false);
            examination.setPenalty(true);

            JavaMailSenderImpl mailSender = getJMS();
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("apoteka@gmail.com");
            mailMessage.setTo(patient.getEmail());
            mailMessage.setSubject("Zakazivanje termina");
            mailMessage.setText("Postovani " + patient.getFirstName() + ",\n"+ "\n"+
                    "Uspesno ste zakazali termin za " + examination.getDate() +
                    " kod lekara" + examination.getDermatologist().getFirstName() + "  " + examination.getDermatologist().getLastName() +  "\n"+ "\n"+
                    "Pozdrav," + "\n"+
                    "AP tim");
            mailSender.send(mailMessage);

            examinationRepository.saveAndFlush(examination);
        }
    }

    @Override
    public boolean cancelExamination(ExaminationDto examinationDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Patient patient = (Patient) authentication.getPrincipal(); --> ovo radi, ali ne povuce allergicMedicines jer je lazy
        Optional<Patient> patientOptional = patientRepository.findById(((User) authentication.getPrincipal()).getId());
        if(patientOptional.isPresent()) {
            Examination examination = examinationMapper.bean2Entity(examinationDto);
            Optional<Examination> examination1 = examinationRepository.findById(examinationDto.getId());
            examination = examination1.get();

            Patient patient = patientOptional.get();
            long helper;
            helper = examination.getDate().getTime();
            System.out.println(helper);
            System.out.println(System.currentTimeMillis());
            if ((helper - 86400000) > System.currentTimeMillis()) {
                examination.setPatient(null);
                examination.setFree(true);
                System.out.println(System.currentTimeMillis());
                examinationRepository.saveAndFlush(examination);
                return true;
            }



            //examinationRepository.saveAndFlush(examination);
        }
        return false;
    }

    @Override
    public Collection<Patient> getAll() {
        return patientRepository.findAll();
    }


    @Override
    public Collection<Medicine> getAllAllergyForPatient(String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Optional<Patient> patientOptional = patientRepository.findById(((User) authentication.getPrincipal()).getId());
        System.out.println(((User) authentication.getPrincipal()).getId());
        Patient patient = patientOptional.get();
        return patient.getAllergicMedicines();

    }

    @Override
    public LoyaltyProgram getLoyaltyProgramForPatient(String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<Patient> patientOptional = patientRepository.findById(((User) authentication.getPrincipal()).getId());
        System.out.println(((User) authentication.getPrincipal()).getId());
        Patient patient = patientOptional.get();
        LoyaltyProgram loyaltyProgram = loyaltyProgramRepository.findByLoyaltyScore(patient.getLoyaltyScore());

        System.out.println(loyaltyProgram.getId());
        patient.setLoyaltyProgram(loyaltyProgram);
        patientRepository.saveAndFlush(patient);
        loyaltyProgram.getPatients().add(patient);
        loyaltyProgramRepository.saveAndFlush(loyaltyProgram);
        return patient.getLoyaltyProgram();

    }

    @Override
    public void addCounseling(CounselingDTO counselingDto){
        System.out.println("Servis");
        Counseling counseling = counselingMapper.bean2Entity(counselingDto);
        System.out.println("Maper");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<Patient> patientOptional = patientRepository.findById(((User) authentication.getPrincipal()).getId());
        System.out.println(((User) authentication.getPrincipal()).getId());
        Patient patient = patientOptional.get();
        counseling.setPatient(patient);
        counseling.setPharmacist(pharmacistRepository.getOne(counselingDto.getPharmacistId()));
        System.out.println(patient);
        System.out.println(counseling);
        int hours = Integer.parseInt(counselingDto.getTime().substring(0,2));
        int minutes = Integer.parseInt(counselingDto.getTime().substring(3));
        Date dateAddTime = counselingDto.getDate();
        dateAddTime.setHours(hours);
        dateAddTime.setMinutes(minutes);
        counseling.setDate(dateAddTime);
        counseling.setDurationMinutes(30);

        Date startDate = new Date();
        startDate.setTime(dateAddTime.getTime());
        startDate.setHours(1);
        Date nextDay =  new Date();
        nextDay.setTime(dateAddTime.getTime());
        nextDay.setHours(23);
        Date endDate = dateAddTime;
        endDate.setMinutes(dateAddTime.getMinutes()+counseling.getDurationMinutes());

        Collection<Counseling> counselingsOnThatDay = counselingRepository.findAllByDateBetween(startDate, nextDay);
        for (Counseling exa: counselingsOnThatDay) {
            Date exaDate = exa.getDate();
            Date exaStart =  new Date();
            exaStart.setTime(exaDate.getTime());
            Date exaEnd =  new Date();
            exaEnd.setTime(exaDate.getTime());
            exaEnd.setMinutes(exa.getDate().getHours()+ exa.getDurationMinutes());
            if (dateAddTime.after(exaStart) && dateAddTime.before(exaEnd)){
                throw new ResourceConflictException(1L,"Preklapa se sa terminom!");
            }
            if(endDate.after(exaStart) && endDate.before(exaEnd)){
                throw new ResourceConflictException(1L,"Preklapa se sa terminom!");
            }
        }

        JavaMailSenderImpl mailSender = getJMS();
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("apoteka@gmail.com");
        mailMessage.setTo(patient.getEmail());
        mailMessage.setSubject("Zakazivanje termina");
        mailMessage.setText("Postovani " + patient.getFirstName() + ",\n"+ "\n"+
                "Uspesno ste zakazali termin za farmaceuta " + counseling.getDate() +
                " kod lekara" + counseling.getPharmacist().getFirstName() + "  " + counseling.getPharmacist().getLastName() +  "\n"+ "\n"+
                "Pozdrav," + "\n"+
                "AP tim");
        mailSender.send(mailMessage);

        counselingRepository.saveAndFlush(counseling);


    }

    @Override
    public boolean cancelCounseling(CounselingDTO counselingDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Patient patient = (Patient) authentication.getPrincipal(); --> ovo radi, ali ne povuce allergicMedicines jer je lazy
        Optional<Patient> patientOptional = patientRepository.findById(((User) authentication.getPrincipal()).getId());
        if(patientOptional.isPresent()) {
            Counseling counseling = counselingMapper.bean2Entity(counselingDto);
            Optional<Counseling> counseling1 = counselingRepository.findById(counselingDto.getId());
            counseling = counseling1.get();

            Patient patient = patientOptional.get();
            long helper;
            helper = counseling.getDate().getTime();
            System.out.println(helper);
            System.out.println(System.currentTimeMillis());
            if ((helper - 86400000) > System.currentTimeMillis()) {
                counseling.setPatient(null);
                counseling.setFree(true);
                System.out.println(System.currentTimeMillis());
                counselingRepository.saveAndFlush(counseling);
                return true;
            }



            //examinationRepository.saveAndFlush(examination);
        }
        return false;
    }

    @Override
    public void addReservation(ReservationDTO reservationDto){
        Reservation reservation = reservationMapper.bean2Entity(reservationDto);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<Patient> patientOptional = patientRepository.findById(((User) authentication.getPrincipal()).getId());
        if(patientOptional.isPresent()) {
            Optional<Pharmacy> pharmacy= pharmacyRepository.findById((long) reservationDto.getPharmacyid());
            reservation.setPharmacy(pharmacy.get());
            Optional<Medicine> medicine= medicineRepository.findById((long) reservationDto.getMedicineid());
            reservation.setMedicine(medicine.get());
            reservation.setEndDate(reservationDto.getEndDate());
            reservation.setPickUpTime(reservationDto.getPickedUpTime());
            reservation.setEndTime(reservationDto.getEndTime());
            reservation.setPatient(patientOptional.get());
            Patient patient = patientOptional.get();

            reservationRepository.saveAndFlush(reservation);

            Reservation reservation1 = reservationRepository.findByPharmacyAndMedicineAndPatient(pharmacy.get(),medicine.get(),patient);

            JavaMailSenderImpl mailSender = getJMS();
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("apoteka@gmail.com");
            mailMessage.setTo(patient.getEmail());
            mailMessage.setSubject("Rezervisanje leka");
            mailMessage.setText("Postovani " + patient.getFirstName() + ",\n"+ "\n"+
                    "Uspesno ste rezervisali lek " + reservation.getMedicine().getName() +
                    " broj rezervacije" + reservation1.getId() +  "\n"+ "\n"+
                    "Pozdrav," + "\n"+
                    "AP tim");
            mailSender.send(mailMessage);

            MedicineQuantityPharmacy mqp = medicineQuantityPharmacyRepository.findAllByPharmacyAndMedicine(pharmacy.get(),medicine.get());
            mqp.setQuantity(mqp.getQuantity() - 1);

            medicineQuantityPharmacyRepository.saveAndFlush(mqp);



        }




    }

    @Override
    public boolean cancelReservation(ReservationDTO reservationDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Patient patient = (Patient) authentication.getPrincipal(); --> ovo radi, ali ne povuce allergicMedicines jer je lazy
        Optional<Patient> patientOptional = patientRepository.findById(((User) authentication.getPrincipal()).getId());
        if(patientOptional.isPresent()) {
            Reservation reservation = reservationMapper.bean2Entity(reservationDto);
            Optional<Reservation> reservation1 = reservationRepository.findById(reservationDto.getId());
            reservation = reservation1.get();

            Patient patient = patientOptional.get();
            long helper;
            helper = reservation.getEndDate().getTime();
            System.out.println(helper);
            System.out.println(System.currentTimeMillis());
            if ((helper - 86400000) > System.currentTimeMillis()) {


                MedicineQuantityPharmacy mqp = medicineQuantityPharmacyRepository.findAllByPharmacyAndMedicine(reservation.getPharmacy(),reservation.getMedicine());
                mqp.setQuantity(mqp.getQuantity() + 1);

                medicineQuantityPharmacyRepository.saveAndFlush(mqp);
                reservation.setPatient(null);
                reservationRepository.saveAndFlush(reservation);

                return true;
            }



            //examinationRepository.saveAndFlush(examination);
        }
        return false;
    }
}
