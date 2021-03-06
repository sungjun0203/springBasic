package com.jun.meeting.Service;

import com.jun.meeting.Dao.FileDao;
import com.jun.meeting.Dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IONCOMMUNICATIONS on 2017-07-25.
 */

@Service
public class UserSignUpService {

    @Autowired
    private UserDao userDao;

    @Autowired
    FileService fileService;

    @Autowired
    FileDao fileDao;

    @Autowired
    public JavaMailSender emailSender;

    private static String CODE_USERTYPE_01 = "일반사용자";
    private static String CODE_USERTYPE_02 = "관리자";
    private static Integer DUPLICATE = 1;
    private static Integer NOTDUPLICATE = 0;


    public HashMap<String,Object> emailCheck(HttpServletRequest request) {

        HashMap<String, Object> resultData = new HashMap<String, Object>();
        String resultString = null;
        String email =null;
        int randomNum=0;
        email = request.getParameter("email");

        System.out.println((userDao.userEmailDuplicateCheck(email)));

        if (!email.endsWith("@dankook.ac.kr")) {
            resultString = "notDANKOOK";
        } else if (userDao.userEmailDuplicateCheck(email) > 0) {
            resultString = "duplicate";
        } else {

            Random rand = new Random();
            int min = 10000;
            int max = 100000;
            randomNum = rand.nextInt(max - min + 1) + min;

            String subject = "DKU Meeting System 이메일 인증";
            String text = "DKU Meeting System 이메일 인증번호 : '" + randomNum + "' 를입력해주세요";

            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(email);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);

            resultString = "sendOK";
        }
        resultData.put("resultString",resultString);
        resultData.put("randomNum",randomNum);
        return resultData;
    }




    public HashMap<String,Object> nicknameCheck(HttpServletRequest request) {

        HashMap<String,Object> resultData = new HashMap<String,Object>();
        String nickname = request.getParameter("nickName");
        String resultString = null;
        if(userDao.userNicknameDuplicateCheck(nickname)==DUPLICATE){
            resultString="a";
        }
        else
            resultString="notDuplicate";

        resultData.put("resultData",resultString);

        return resultData;
    }


    public void userSignUpSuccess(HttpServletRequest request) {

//-----------------------------------------------------------------------------------------------------------------------------

        long dateTime = System.currentTimeMillis();  // 또는 System.nanoTime();
        SimpleDateFormat date = new SimpleDateFormat("yyyy:MM:dd-hh:mm:ss");
        String stringDateTime = date.format(new Date(dateTime));

        String email = request.getParameter("inputEmail");
        String password = request.getParameter("inputPassword"); // null
        String name = request.getParameter("inputName");
        String birth = request.getParameter("inputBirthDay");
        String gender = request.getParameter("gender");
        String college = request.getParameter("inputCollege");
        String major = request.getParameter("inputMajor"); // 수정
        String phone = request.getParameter("inputNumber");
        String nickName = request.getParameter("inputNickName");

        HashMap<String, Object> userInformation = new HashMap<String, Object>();

        userInformation.put("email", email);
        userInformation.put("password", password);
        userInformation.put("name", name);
        userInformation.put("birth", birth);
        userInformation.put("gender", gender);
        userInformation.put("college", college);
        userInformation.put("major", major);
        userInformation.put("phone", phone);
        userInformation.put("signUpDate", stringDateTime);
        userInformation.put("userType", CODE_USERTYPE_01);
        userInformation.put("nickName", nickName);

        System.out.println(userInformation);
        userDao.userSignUp(userInformation);


        String path = "c://aaa";

        Map fileInfo = null;
        HashMap<String, Object> fileInformation = new HashMap<String, Object>();

        try {

            fileInfo = fileService.fileUpload(request, path); //해당 디렉토리 위치에 파일 업로드
            List files = (List) fileInfo.get("files");

            if (files.size() > 0) {

                fileInformation.put("file_gbn", "BD");

                for (int i = 0; i < files.size(); i++) {
                    Map file = (Map) files.get(i);

                    String origName = (String) file.get("origName");
                    File sFile = (File) file.get("sfile");

                    fileInformation.put("file_name", sFile.getName()); //복호화된 파일 이름
                    fileInformation.put("file_path", sFile.getAbsolutePath()); //물리적 저장 경로
                    fileInformation.put("file_size", sFile.length()); //파일 크기
                    fileInformation.put("file_orig", origName); //원래 파일 명
                    fileInformation.put("userEmail", email);
                    fileDao.insertFileToDB(fileInformation);
                }
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}
