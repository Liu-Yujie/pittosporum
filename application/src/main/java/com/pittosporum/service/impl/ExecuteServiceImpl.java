package com.pittosporum.service.impl;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.pittosporum.constant.AppErrorCode;
import com.pittosporum.dao.StoreDao;
import com.pittosporum.service.ExecuteService;
import com.pittosporum.util.SQLPropertiesParseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pittosporum.constant.PittosporumException;
import pittosporum.constant.Status;
import pittosporum.core.ProcessResponse;
import pittosporum.core.SQLProperties;
import pittosporum.entity.SQLStore;
import pittosporum.utils.DataSourceManager;
import pittosporum.utils.JDBCTemplateHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author yichen(graffitidef @ gmail.com)
 */

@Service
@Slf4j
public class ExecuteServiceImpl implements ExecuteService {

    @Autowired
    private StoreDao dao;

    @Override
    @Transactional
    public ProcessResponse<Void> executeSqlByStoreId(String storeId) {
        SQLStore sqlStore = dao.selectSqlStoreById(storeId);
        if (sqlStore == null){
            log.info("====>>>>>> can not find sql by id", storeId);
            return ProcessResponse.failure(AppErrorCode.EMPTY_OBJECT.getStatusCode(), AppErrorCode.EMPTY_OBJECT.getMessage());
        }

        SQLProperties sqlProperties = SQLPropertiesParseUtil.parseToSQLProperties(sqlStore);
        execute(sqlProperties);
        return ProcessResponse.success();
    }

    @Override
    public ProcessResponse<Void> executeSqlList(List<String> storeIds) {
        List<SQLStore> sqlStoreList = new ArrayList<>();
        for (String s : storeIds){
            SQLStore e = dao.selectSqlStoreById(s);
            if (e != null){
                sqlStoreList.add(e);
            }
        }

        PriorityQueue<SQLProperties> executeQueue = SQLPropertiesParseUtil.parseToSQLPropertiesList(sqlStoreList);

        Iterator<SQLProperties> itr = executeQueue.iterator();
        while (itr.hasNext()){
            SQLProperties e = itr.next();

            execute(e);

        }

        return ProcessResponse.success();
    }



    private void execute(SQLProperties sqlProperties) throws PittosporumException{
        if (sqlProperties == null){
            return;
        }

        try {
            String exSql = sqlProperties.getSql();
            String profileName = sqlProperties.getProfileName();
            ComboPooledDataSource comboPooledDataSource = DataSourceManager.getDriverManagerDataSourceByName(profileName + "DataSource");
            JdbcTemplate jdbcTemplate = JDBCTemplateHelper.getJdbcTemplateByDataSource(comboPooledDataSource);
            jdbcTemplate.update(exSql);
            dao.changeRunStatus(sqlProperties.getStoreId(), Status.EXECUTE_OVER);
        }catch (Exception e){
            log.error("========>>>>executeSqlByStoreId>>>>>>>>>>>>>", e);
            dao.changeRunStatus(sqlProperties.getStoreId(), Status.EXECUTE_FAILURE);
        }

    }
}
