package com.productpractice.practice2.web.form;

import com.productpractice.practice2.domain.common.file.UploadFile;
import lombok.Data;

import java.util.List;

@Data
public class DetailForm {
  private Long productId;   //  PRODUCT_ID	NUMBER(10,0)
  private String pname;     //  PNAME	VARCHAR2(30 BYTE)
  private Long quantity;    //  QUANTITY	NUMBER(10,0)
  private Long price;       //  PRICE	NUMBER(10,0)

  private UploadFile attachFile;
  private List<UploadFile> imageFiles;
}
