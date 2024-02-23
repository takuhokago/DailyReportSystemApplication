package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Employee.Role;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

 // 従業員一覧表示処理
    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    // 1件を検索
    public Report findById(String id) {
        // findByIdで検索
        Optional<Report> option = reportRepository.findById(id);
        // 取得できなかった場合はnullを返す
        Report report = option.orElse(null);
        return report;
    }

    // 日報一覧に表示する日報を取得
    public List<Report> find(UserDetail userDetail) {
        // ログインしているユーザーのEmployeeを取得
        Employee employee = userDetail.getEmployee();

        if(employee.getRole().equals(Role.ADMIN)) {
            return reportRepository.findAll();
        } else {
            return findByEmployee(employee);
        }

    }

    public List<Report> findByEmployee(Employee employee) {
        List<Report> reports = reportRepository.findByEmployee(employee);
        return reports;
    }

    // 日報保存
    @Transactional
    public ErrorKinds save(Report report, UserDetail userDetail) {
        // ログインしているユーザーのEmployeeを取得
        Employee employee = userDetail.getEmployee();

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        // 日付重複チェック
        ErrorKinds result = reportDateCheck(report, employee);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // ログインしているユーザーのEmployeeをReportに登録
        report.setEmployee(employee);

        reportRepository.save(report);

        return ErrorKinds.SUCCESS;

    }


 // 日報更新
    @Transactional
    public ErrorKinds update(Report report) {

        Employee employee = report.getEmployee();

        // 日付重複チェック
        ErrorKinds result = reportDateCheck(report, employee);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(findById(report.getId().toString()).getCreatedAt());
        report.setUpdatedAt(now);

        reportRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

 // 日報削除
    @Transactional
    public void delete(String id) {

        Report report= findById(id);
        LocalDateTime now = LocalDateTime.now();
        report.setUpdatedAt(now);
        report.setDeleteFlg(true);
    }

    // 日付重複チェック
    private ErrorKinds reportDateCheck(Report report, Employee employee) {
        // 引数employeeの日報を取得
        List<Report> reports = reportRepository.findByEmployee(employee);

        if(reports != null) {
            for(Report rep : reports) {
                if(rep.getReportDate().isEqual(report.getReportDate())) {
                    // TODO
                    if(report.getId() != null) {
                        if(report.getId().equals(rep.getId())) {
                            // 同一idの日報の場合日付同じでも無問題
                            continue;
                        }
                    }
                    // 同一の日報でない場合かつ同じ日付の日報がある場合エラーを返す
                    return ErrorKinds.DATECHECK_ERROR;
                }
            }
        }

        return ErrorKinds.CHECK_OK;
    }





}