/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.model.rowMapper;

import cc.altius.FASP.model.BusinessFunction;
import cc.altius.FASP.model.Label;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author altius
 */
public class BusinessFunctionRowMapper implements RowMapper<BusinessFunction> {

    @Override
    public BusinessFunction mapRow(ResultSet rs, int i) throws SQLException {
        BusinessFunction b = new BusinessFunction();
        b.setBusinessFunctionId(rs.getString("BUSINESS_FUNCTION_ID"));
        b.setLabel(new Label(rs.getInt("LABEL_ID"), rs.getString("LABEL_EN"), rs.getString("LABEL_SP"), rs.getString("LABEL_FR"), rs.getString("LABEL_PR")));
        return b;
    }

}
