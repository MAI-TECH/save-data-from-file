package com.maitech.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maitech.models.UserModel;
import com.maitech.repositories.UserRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserModel> findUsers() {
        return userRepository.findAll();
    }

    public boolean saveDataFromUploadFile(MultipartFile file) {
        boolean isFlag = false;
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());

        assert extension != null;
        if (extension.equalsIgnoreCase("json")) {
            isFlag = readDataFromJSONFile(file);
        } else if (extension.equalsIgnoreCase("cvs")) {
            isFlag = readDataFromCSVFile(file);
        } else if (extension.equalsIgnoreCase("xls") || extension.equalsIgnoreCase("xlsx")) {
            isFlag = readDataFromExcelFile(file);
        }

        return isFlag;
    }

    private boolean readDataFromJSONFile(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            List<UserModel> userModels = Arrays.asList(mapper.readValue(inputStream, UserModel[].class));

            if (userModels != null && userModels.size() > 0) {
                for (UserModel userModel : userModels) {
                    userModel.setFileType(FilenameUtils.getExtension(file.getOriginalFilename()));
                    userRepository.save(userModel);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean readDataFromCSVFile(MultipartFile file) {
        try {
            InputStreamReader reader = new InputStreamReader(file.getInputStream());
            CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();

            List<String[]> rows = csvReader.readAll();

            for (String[] row : rows) {
                userRepository.save(new UserModel(row[0], row[1], row[2], row[3], FilenameUtils.getExtension(file.getOriginalFilename())));
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean readDataFromExcelFile(MultipartFile file) {
        Workbook workbook = getWorkBook(file);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();
        rows.next();

        while (rows.hasNext()) {
            Row row = rows.next();
            UserModel userModel = new UserModel();

            if (row.getCell(0).getCellType() == CellType.STRING) {
                userModel.setLastName(row.getCell(0).getStringCellValue());
            }
            if (row.getCell(1).getCellType() == CellType.STRING) {
                userModel.setFirstName(row.getCell(1).getStringCellValue());
            }
            if (row.getCell(2).getCellType() == CellType.STRING) {
                userModel.setEmail(row.getCell(2).getStringCellValue());
            }
            if (row.getCell(3).getCellType() == CellType.NUMERIC) {
                String phoneNumber = NumberToTextConverter.toText(row.getCell(3).getNumericCellValue());
                userModel.setPhoneNumber(phoneNumber);
            } else if (row.getCell(3).getCellType() == CellType.STRING) {
                userModel.setPhoneNumber(row.getCell(3).getStringCellValue());
            }

            userModel.setFileType(FilenameUtils.getExtension(file.getOriginalFilename()));
            userRepository.save(userModel);
        }
        return true;
    }

    private Workbook getWorkBook(MultipartFile file) {
        Workbook workbook = null;
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        try {
            if (extension.equalsIgnoreCase("xlsx")) {
                workbook = new XSSFWorkbook(file.getInputStream());
            } else if (extension.equalsIgnoreCase("xls")) {
                workbook = new HSSFWorkbook(file.getInputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return workbook;
    }
}
