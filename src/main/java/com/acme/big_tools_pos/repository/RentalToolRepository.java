package com.acme.big_tools_pos.repository;


import com.acme.big_tools_pos.dto.CodeRentalCharge;
import com.acme.big_tools_pos.model.LuCode;
import org.springframework.stereotype.Repository;


public interface RentalToolRepository {
    public CodeRentalCharge findByCode(LuCode.Name code);
}
