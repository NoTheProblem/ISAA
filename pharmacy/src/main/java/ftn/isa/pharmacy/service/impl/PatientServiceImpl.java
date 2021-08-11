package ftn.isa.pharmacy.service.impl;

import ftn.isa.pharmacy.config.MailConfig;
import ftn.isa.pharmacy.dto.CounselingDTO;
import ftn.isa.pharmacy.dto.ExaminationDto;
import ftn.isa.pharmacy.dto.MedicineDto;
import ftn.isa.pharmacy.mapper.CounselingMapper;
import ftn.isa.pharmacy.mapper.ExaminationMapper;
import ftn.isa.pharmacy.mapper.MedicineMapper;
import ftn.isa.pharmacy.model.*;
import ftn.isa.pharmacy.repository.*;
import ftn.isa.pharmacy.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    @Autowired
    public PatientServiceImpl(PharmacistRepository pharmacistRepository, CounselingRepository counselingRepository, CounselingMapper counselingMapper, MailConfig mailConfig, MedicineMapper medicineMapper, PatientRepository patientRepository, LoyaltyProgramRepository loyaltyProgramRepository, ExaminationMapper examinationMapper, ExaminationRepository examinationRepository) {
        this.medicineMapper = medicineMapper;
        this.patientRepository = patientRepository;
        this.loyaltyProgramRepository = loyaltyProgramRepository;
        this.examinationMapper = examinationMapper;
        this.examinationRepository = examinationRepository;
        this.mailConfig = mailConfig;
        this.counselingMapper = counselingMapper;
        this.counselingRepository = counselingRepository;
        this.pharmacistRepository = pharmacistRepository;
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
}
