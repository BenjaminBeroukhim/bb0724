package com.acme.big_tools_pos.service;


import com.acme.big_tools_pos.BigToolsPosApplication;
import com.acme.big_tools_pos.dto.CodeRentalCharge;
import com.acme.big_tools_pos.dto.RentalAgreement;
import com.acme.big_tools_pos.model.DateFunction;
import com.acme.big_tools_pos.model.LuCode;
import com.acme.big_tools_pos.model.LuYesNo;
import com.acme.big_tools_pos.repository.RentalToolRepositoryImpl;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * RentalToolService encapsulates the App's business logic into a single place. It uses the Repository to access
 * data to implement the business logic. It is called by Main Class.
 */
@Data
@Service
public class RentalToolServiceImpl implements RentalToolService {

    //Logback-spring Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(BigToolsPosApplication.class);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private RentalToolRepositoryImpl rentalToolRepository;

    public RentalToolServiceImpl() {}

    //Main acting as IOC injects the reference to the Repository layer
    @Autowired
    public RentalToolServiceImpl(RentalToolRepositoryImpl rentalToolRepository) {
        this.rentalToolRepository = rentalToolRepository;
    }

    //Generate the RentalAgreement logic by accessing the Repository.
    @Override
    public RentalAgreement generateRentalAgreementByCode(String toolCode, int rentalDayCount, int discountPercent,
                                                         LocalDate checkOutDate) {

        RentalAgreement rentalAgreement = new RentalAgreement();

        try {
            //get the result of the join of the tables from the Repository layer.
            LuCode.Name codeName = LuCode.Name.valueOf(toolCode);
            CodeRentalCharge codeRentalCharge = rentalToolRepository.findByCode(codeName);

            //Date definitions
            //LocalDate today = LocalDate.of(2024, 7, 7);
            //LocalDate today = LocalDate.now();
            LocalDate startDate = checkOutDate.plusDays(1);
            LocalDate endDate = checkOutDate.plusDays(rentalDayCount + 1);

            // Calculate different days counts
            long rentalDays = ChronoUnit.DAYS.between(startDate, endDate);
            long weekDays = DateFunction.calcWeekDays(startDate, endDate);
            long weekends = rentalDays - weekDays;
            long holidays = DateFunction.calcHolidaysBetween(startDate, endDate);

            //Populate the rentalAgreement object
            rentalAgreement.setToolCode(toolCode);
            rentalAgreement.setToolType(codeRentalCharge.getToolType().name());
            rentalAgreement.setToolBrand(codeRentalCharge.getToolBrand().name());
            rentalAgreement.setRentalDays(Long.toString(rentalDays));
            rentalAgreement.setCheckOutDate(checkOutDate.format(formatter));

            //Calculate the Charge by looking at different charge definitions.
            rentalAgreement.setDueDate(endDate.format(formatter));
            rentalAgreement.setDailyRentalCharge(String.format("%.2f", codeRentalCharge.getDailyCharge()));
            long chargeDays = 0;

            rentalAgreement.setWeekendCharge(false);
            rentalAgreement.setHolidayCharge(true);
            if (codeRentalCharge.getWeekdayCharge() == LuYesNo.Name.Yes) {
                chargeDays = weekDays;
            }
            if (codeRentalCharge.getHolidayCharge() == LuYesNo.Name.No) {
                chargeDays -= holidays;
                rentalAgreement.setHolidayCharge(false);
            }
            if (codeRentalCharge.getWeekendCharge() == LuYesNo.Name.Yes) {
                chargeDays += weekends;
                rentalAgreement.setWeekendCharge(true);
            }
            rentalAgreement.setChargeDays(Long.toString(chargeDays));

            //Calculate the discountAmouont and the finalCharge
            Double preDiscountCharge = codeRentalCharge.getDailyCharge() * chargeDays;
            rentalAgreement.setPreDiscountCharge(String.format("%.2f", preDiscountCharge));

            rentalAgreement.setDiscountPercent(Integer.toString(discountPercent));

            Double discountAmount = (Double) (discountPercent / 100.0) * preDiscountCharge;
            discountAmount = (int) (Math.round(discountAmount * 100)) / 100.0;
            rentalAgreement.setDiscountAmount(String.format("%.2f", discountAmount));
            Double finalCharge = (int) (Math.round((preDiscountCharge - discountAmount) * 100)) / 100.0;
            rentalAgreement.setFinalCharge(String.format("%.2f", finalCharge));

        } catch (Exception e) {
            throw e;
        }
        return rentalAgreement;
    }
}
