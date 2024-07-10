package com.acme.big_tools_pos;

/**
 * Main class
 *  GUI which retrieves the user data and calls Service class to present the results to the user.
*/

import com.acme.big_tools_pos.dto.RentalAgreement;
import com.acme.big_tools_pos.model.LuCode;
import com.acme.big_tools_pos.repository.RentalToolRepositoryImpl;
import com.acme.big_tools_pos.service.RentalToolService;
import com.acme.big_tools_pos.service.RentalToolServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

@SpringBootApplication
public class BigToolsPosApplication implements CommandLineRunner {

	//Logback-spring Logger
	private static final Logger LOGGER = LoggerFactory.getLogger(BigToolsPosApplication.class);

	@Autowired
	RentalToolService rentalToolService;

	public static void main(String[] args) {
		SpringApplication.run(BigToolsPosApplication.class, args);
	}

	@Override
	public void run (String... args) {

		//Start of GUI to get the values for ToolCode, RentalDayCount, DiscountPercent and CheckOutDate,
		System.out.println("***************************** TOOL RENTAL APPLICATION ******************************");
		boolean isExit = false;
		while (!isExit) {
			try {
				System.out.println("Please choose a Tool Code!");
				int i = 1;
				for (LuCode.Name code : LuCode.Name.values()) {
					System.out.println(i + " -> " + code);
					i++;
				}
				System.out.println("0 -> Exit");
				System.out.println("****************");

				Scanner input = new Scanner(System.in);
				int chosen = input.nextInt();
				if (chosen > LuCode.Name.values().length || chosen < 0) {
					System.out.println("Invalid selection, Please try again");
					continue;
				}

				if (chosen == 0) {
					isExit = true;
					continue;
				}

				//Read ToolCode
				String toolCode = LuCode.Name.values()[chosen - 1].name();

				//Read RentalDayCount
				String prompt = "Please Enter Rental Day Count: ";
				int rentalDayCount = nextIntNotNegative(prompt, false);

				//Read DiscountPercent
				int discountPercent = -1;
				for(;;) {
					prompt = "Please Enter Discount Percent (Value between 0 and 100): ";
					discountPercent = nextIntNotNegative(prompt, true);
					if (discountPercent > 100) {
						System.out.println("Error: value greater than 100.");
						continue;
					}
					break;
				}

				//Read CheckOutDate
				LocalDate today = LocalDate.now();
				String todayStr = today.format(DateTimeFormatter.ofPattern("M/d/yy" ));
				prompt = "Please Enter Check out date: (example: today's date " + todayStr + ")";
				LocalDate checkOutDate = nextStringValidDate(prompt);

                /*
                String toolCode = "JAKD";
                int rentalDayCount = 100 + 365;
                int discountPercent = 20 ;
                */
				//call the generateRentalAgreementByCode provided by Service
				RentalAgreement rentalAgreement = rentalToolService.generateRentalAgreementByCode(toolCode, rentalDayCount,
						discountPercent, checkOutDate);
				System.out.println("***** RENTAL AGREEMENT *****");
				System.out.println("ToolCode: \"" + rentalAgreement.getToolCode() + "\"");
				System.out.println("ToolType: \"" + rentalAgreement.getToolType() + "\"");
				System.out.println("ToolBrand: \"" + rentalAgreement.getToolBrand() + "\"");
				System.out.println("RentalDays: " + rentalAgreement.getRentalDays());
				System.out.println("CheckOutDate: " + rentalAgreement.getCheckOutDate());
				System.out.println("DueDate: " + rentalAgreement.getDueDate());
				System.out.println("DailyRentalCharge: $" + rentalAgreement.getDailyRentalCharge());
				System.out.println("ChargeDays: " + rentalAgreement.getChargeDays());
				if (rentalAgreement.isWeekendCharge() == true && rentalAgreement.isHolidayCharge() == true) {
					System.out.println("Info: Weekend and Holiday are charged");
				}
				if (rentalAgreement.isWeekendCharge() == true && rentalAgreement.isHolidayCharge() == false) {
					System.out.println("Info: Weekends are charged but Holidays are not charged");
				}
				if (rentalAgreement.isWeekendCharge() == false && rentalAgreement.isHolidayCharge() == true) {
					System.out.println("Info: Weekends are not charged but Holidays are charged");
				}
				if (rentalAgreement.isWeekendCharge() == false && rentalAgreement.isHolidayCharge() == false) {
					System.out.println("Info: Weekends and Holidays are not charged");
				}
				System.out.println("PreDiscountCharge: $" + rentalAgreement.getPreDiscountCharge());
				System.out.println("DiscountPercent: " + rentalAgreement.getDiscountPercent());
				System.out.println("DiscountAmount: $" + rentalAgreement.getDiscountAmount());
				System.out.println("FinalCharge: $" + rentalAgreement.getFinalCharge());
				System.out.println("****************");
				logAgremment(rentalAgreement);
			} //try
			catch(Exception e){
				//log the exception
				LOGGER.info(e.getMessage());
				System.out.println(e.getMessage());
			}
		} //while
	}

	/*
		Read a positive Integer.
        prompt: prompt to the user
        includeZero: if zero is acceptable
	*/
	public static int nextIntNotNegative(String prompt, boolean includeZero) {

		Scanner input = new Scanner(System.in);
		while (true) {
			try {
				System.out.println(prompt);
				int n = Integer.parseInt(input.nextLine().trim());
				if (includeZero) {
					if (n >= 0)
						return n;
					else
						throw new IllegalArgumentException();
				}
				else {
					if (n > 0)
						return n;
					else
						throw new IllegalArgumentException();
				}
			} catch (NumberFormatException e) {
				throw e;
			} catch (IllegalArgumentException iae) {
				throw iae;
			}
		}
	}
	/*
		Read a valid date.
		prompt: prompt to the user
	*/
	public static LocalDate nextStringValidDate(String prompt) {
		while (true) {
			try {
				System.out.println(prompt);
				Scanner scanner = new Scanner(System.in);
				String dateStr = scanner.next();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy");
				LocalDate date = LocalDate.parse(dateStr, formatter);
				LocalDate today = LocalDate.now();
				int compareValue = today.compareTo(date);
				if (compareValue > 0) {
					System.out.println("Error: Checkout Date must be grater than today");
				} else {
					return date;
				}
			}
			catch (DateTimeParseException e) {
				throw e;
			}
		}
	}
	public static void logAgremment(RentalAgreement rentalAgreement) {
		LOGGER.info("***** RENTAL AGREEMENT *****");
		LOGGER.info("ToolCode: \"" + rentalAgreement.getToolCode() + "\"");
		LOGGER.info("ToolType: \"" + rentalAgreement.getToolType() + "\"");
		LOGGER.info("ToolBrand: \"" + rentalAgreement.getToolBrand() + "\"");
		LOGGER.info("RentalDays: " + rentalAgreement.getRentalDays());
		LOGGER.info("CheckOutDate: " + rentalAgreement.getCheckOutDate());
		LOGGER.info("DueDate: " + rentalAgreement.getDueDate());
		LOGGER.info("DailyRentalCharge: $" + rentalAgreement.getDailyRentalCharge());
		LOGGER.info("ChargeDays: " + rentalAgreement.getChargeDays());
		if (rentalAgreement.isWeekendCharge() == true && rentalAgreement.isHolidayCharge() == true) {
			LOGGER.info("Info: Weekend and Holiday are charged");
		}
		if (rentalAgreement.isWeekendCharge() == true && rentalAgreement.isHolidayCharge() == false) {
			LOGGER.info("Info: Weekends are charged but Holidays are not charged");
		}
		if (rentalAgreement.isWeekendCharge() == false && rentalAgreement.isHolidayCharge() == true) {
			LOGGER.info("Info: Weekends are not charged but Holidays are charged");
		}
		if (rentalAgreement.isWeekendCharge() == false && rentalAgreement.isHolidayCharge() == false) {
			LOGGER.info("Info: Weekends and Holidays are not charged");
		}
		LOGGER.info("PreDiscountCharge: $" + rentalAgreement.getPreDiscountCharge());
		LOGGER.info("DiscountPercent: " + rentalAgreement.getDiscountPercent());
		LOGGER.info("DiscountAmount: $" + rentalAgreement.getDiscountAmount());
		LOGGER.info("FinalCharge: $" + rentalAgreement.getFinalCharge());
		LOGGER.info("****************");
	}
}