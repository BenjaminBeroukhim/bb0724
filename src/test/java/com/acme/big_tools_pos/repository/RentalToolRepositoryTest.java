package com.acme.big_tools_pos.repository;

import com.acme.big_tools_pos.dto.CodeRentalCharge;
import com.acme.big_tools_pos.model.LuBrand;
import com.acme.big_tools_pos.model.LuCode;
import com.acme.big_tools_pos.model.LuType;
import com.acme.big_tools_pos.model.LuYesNo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RentalToolRepositoryTest {

    private RentalToolRepository rtr;

    @Before
    public void setUp() throws Exception {
        rtr = new RentalToolRepositoryImpl();
    }

    // Test finding a valid code and ensuring all fields are retrieved
    @Test
    public void testValidFindByCode() {
        LuCode.Name code = LuCode.Name.CHNS;

        CodeRentalCharge crc = rtr.findByCode(code);
        assertEquals(1.49, crc.getDailyCharge(), 0.01);
        assertEquals(LuYesNo.Name.Yes, crc.getHolidayCharge());
        assertEquals(LuBrand.Name.Stihl, crc.getToolBrand());
        assertEquals(LuType.Name.Chainsaw, crc.getToolType());
        assertEquals(LuYesNo.Name.Yes, crc.getWeekdayCharge());
        assertEquals(LuYesNo.Name.No, crc.getWeekendCharge());
    }
}
