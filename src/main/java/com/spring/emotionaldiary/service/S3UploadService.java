package com.spring.emotionaldiary.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.spring.emotionaldiary.exception.Code;
import com.spring.emotionaldiary.exception.PrivateException;
import com.spring.emotionaldiary.model.Tags;
import com.spring.emotionaldiary.model.response.DefaultRes;
import com.spring.emotionaldiary.model.response.ResponseMessage;
import com.spring.emotionaldiary.model.response.StatusCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor // 변수 final 선언시 사용
public class S3UploadService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Transactional
    public List<String> upload(List<String> imageUrl)
    {
        List<String> images = new ArrayList<>();

        //forEach 구문을 통해 multipartFile로 넘어온 파일들을 하나씩 fileNameList에 추가
        for(String image:imageUrl) {
            String fileName = createFileName(); // 파일 이름 랜덤 생성

            // Base64로 인코딩된 이미지 데이터를 디코딩
            String base64Data = image.replaceFirst("^data:image\\/\\w+;base64,", "");
            byte[] imageData = Base64.decodeBase64(base64Data);

            // putObject() 메소드 : 파일 저장
            amazonS3.putObject(new PutObjectRequest(bucket+"/post/image",fileName,new ByteArrayInputStream(imageData),null)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            // getUrl()를 통해 파일이 저장된 URL을 return
            // 이 URL로 이동 시 해당 파일이 오픈
            images.add(amazonS3.getUrl(bucket+"/post/image",fileName).toString());

        }
        return images;
    }

    // 이미지파일명 중복 방지
    private String createFileName(){
        return UUID.randomUUID().toString();
    }

//    // 파일 유효성 검사
//    private String getFileExtension(String fileName){
//        if(fileName.length() == 0){
//            throw new PrivateException(Code.WRONG_INPUT_IMAGE);
//        }
//        // 이미지파일 관련 확장자만 파일 업로드 할 수 있도록 설정
//        ArrayList<String> fileValidate = new ArrayList<>();
//        fileValidate.add(".jpg");
//        fileValidate.add(".jpeg");
//        fileValidate.add(".png");
//        fileValidate.add(".JPG");
//        fileValidate.add(".JPEG");
//        fileValidate.add(".PNG");
//
//        String idxFileName = fileName.substring(fileName.lastIndexOf("."));
//        if(!fileValidate.contains(idxFileName)){
//            throw new PrivateException(Code.WRONG_IMAGE_FORMAT);
//        }
//        return fileName.substring(fileName.lastIndexOf("."));
//    }

    //이미지 파일 삭제
    @Transactional
    public ResponseEntity delete(String filePath){
        try{
            // S3에서 파일 삭제
            amazonS3.deleteObject(new DeleteObjectRequest(bucket,filePath));
        }catch(AmazonServiceException e){
            e.printStackTrace();
        }catch (SdkClientException e){
            e.printStackTrace();
        }
        return new ResponseEntity(DefaultRes.res(StatusCode.OK,"이미지 파일 삭제 성공",filePath),HttpStatus.OK);
    }
}
