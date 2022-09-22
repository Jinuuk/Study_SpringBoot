package com.kh.demo.web;

import com.kh.demo.domain.common.file.AttachCode;
import com.kh.demo.domain.common.file.UploadFile;
import com.kh.demo.domain.common.file.UploadFileDAO;
import com.kh.demo.domain.product.Product;
import com.kh.demo.domain.product.ProductSVC;
import com.kh.demo.web.form.DetailForm;
import com.kh.demo.web.form.SaveForm;
import com.kh.demo.web.form.UpdateForm;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@AllArgsConstructor
@RequestMapping("/products")
public class ProductController {

  private final ProductSVC productSVC;
  private final UploadFileDAO uploadFileDAO;

  //등록양식
  @GetMapping("/add")
  public String saveForm(Model model) {
    model.addAttribute("form", new SaveForm());
    return "product/saveForm";
  }

//  //등록
//  //@PostMapping("/add")
//  public String saveV2(@Valid @ModelAttribute("form") SaveForm saveForm,
//                     BindingResult bindingResult,
//                     RedirectAttributes redirectAttributes) throws IOException {
//
//    log.info("saveForm={}", saveForm);
//    if (!saveForm.getFile().isEmpty()) {
//      log.info("첨부파일이름={}", saveForm.getFile().getOriginalFilename());
//      log.info("파일크기={}", saveForm.getFile().getSize());
//      log.info("파일유형={}", saveForm.getFile().getContentType());
//      String originalFilename = saveForm.getFile().getOriginalFilename();
//
//      String storedFileName = storedFileName(originalFilename);
//      saveForm.getFile().transferTo(new File("d:/tmp/"+storedFileName));
//
//      log.info("내부보관파일명={}",storedFileName);
//
//
//    }
//
//    if (!saveForm.getFiles().isEmpty()) {
//      List<MultipartFile> files = saveForm.getFiles();
//      files.stream().forEach(file->{
//        log.info("첨부파일이름={}", file.getOriginalFilename());
//        log.info("파일크기={}", file.getSize());
//        log.info("파일유형={}", file.getContentType());
//        String originalFilename = file.getOriginalFilename();
//        String storedFileName = storedFileName(originalFilename);
//        log.info("내부보관파일명={}",storedFileName);
//        try {
//          file.transferTo(new File("d:/tmp/"+storedFileName));
//        } catch (IOException e) {
//          throw new RuntimeException(e);
//        }
//      });
//    }
//
//    return "redirect:/products/{id}/detail";
//  }

  //등록
  @PostMapping("/add")
  public String save(@Valid @ModelAttribute("form") SaveForm saveForm,
                     BindingResult bindingResult,
                     RedirectAttributes redirectAttributes) {

    //기본검증
    if(bindingResult.hasErrors()){
      log.info("bindingResult={}", bindingResult);
      return "product/saveForm";
    }

    //필드검증
    //상품수량은 100초과 금지
    if(saveForm.getQuantity() > 100){
      bindingResult.rejectValue("quantity","product.quantity",new Integer[]{100},"상품수량 초과");
      log.info("bindingResult={}", bindingResult);
      return "product/saveForm";
    }

    //오브젝트검증
    //총액(상품수량*단가) 1000만원 초과금지
    if(saveForm.getQuantity() * saveForm.getPrice() > 10_000_000L){
      bindingResult.reject("product.totalPrice",new Integer[]{1000},"총액 초과!");
      log.info("bindingResult={}", bindingResult);
      return "product/saveForm";
    }

    Product product = new Product();
    BeanUtils.copyProperties(saveForm, product);

    Long productId = 0L;

    //상품
    //주의 : view에서 multiple인 경우 파일 첨부가 없더라도 빈문자열("")이 반환되어
    // List<MultipartFile>에 빈 객체 1개가 포함됨
    if (saveForm.getFile().isEmpty() && saveForm.getFiles().get(0).isEmpty()) {
      productId = productSVC.save(product);
    //상품, 설명 첨부
    } else if (!saveForm.getFile().isEmpty() && saveForm.getFiles().get(0).isEmpty()) {
      productId = productSVC.save(product,saveForm.getFile());
    //상품, 이미지 첨부
    } else if (saveForm.getFile().isEmpty() && !saveForm.getFiles().get(0).isEmpty()) {
      productId = productSVC.save(product,saveForm.getFiles());
    //상품, 설명 첨부, 이미지 첨부
    } else if (!saveForm.getFile().isEmpty() && !saveForm.getFiles().get(0).isEmpty()) {
      productId = productSVC.save(product,saveForm.getFile(),saveForm.getFiles());
    }

    redirectAttributes.addAttribute("id", productId);
    return "redirect:/products/{id}/detail";
  }

  //조회
  @GetMapping("/{id}/detail")
  public String findByProductId(@PathVariable("id") Long productId,
                                Model model) {

    //1)상품조회
    Optional<Product> findedProduct = productSVC.findByProductId(productId);
    DetailForm detailForm = new DetailForm();
    if(!findedProduct.isEmpty()) {
      BeanUtils.copyProperties(findedProduct.get(), detailForm);
    }

    //2)첨부파일 조회
    //2-1)상품설명파일 조회
    List<UploadFile> uploadFile = uploadFileDAO.getFilesByCodeWithRid(AttachCode.P0101.name(), productId);
    if (uploadFile.size()>0) {
      UploadFile attachFile = uploadFile.get(0);
      detailForm.setAttachFile(attachFile);
    }
    //2-2)상품이미지 조회
    List<UploadFile> uploadFiles = uploadFileDAO.getFilesByCodeWithRid(AttachCode.P0102.name(), productId);
    if (uploadFiles.size()>0) {
      List<UploadFile> imageFiles = new ArrayList<>();
      for (UploadFile file:uploadFiles) {
        imageFiles.add(file);
      }
      detailForm.setImageFiles(imageFiles);
    }

    model.addAttribute("form", detailForm);
    return "product/detailForm";
  }

  //수정양식
  @GetMapping("/{id}/edit")
  public String updateForm(@PathVariable("id") Long productId,
                           Model model) {
    //1)상품조회
    Optional<Product> findedProduct = productSVC.findByProductId(productId);
    UpdateForm updateForm = new UpdateForm();
    if(!findedProduct.isEmpty()) {
      BeanUtils.copyProperties(findedProduct.get(), updateForm);
    }

    //2)첨부파일 조회
    //2-1)상품설명파일 조회
    List<UploadFile> uploadFile = uploadFileDAO.getFilesByCodeWithRid(AttachCode.P0101.name(), productId);
    if (uploadFile.size()>0) {
      UploadFile attachFile = uploadFile.get(0);
      updateForm.setAttachFile(attachFile);
    }
    //2-2)상품이미지 조회
    List<UploadFile> uploadFiles = uploadFileDAO.getFilesByCodeWithRid(AttachCode.P0102.name(), productId);
    if (uploadFiles.size()>0) {
      List<UploadFile> imageFiles = new ArrayList<>();
      for (UploadFile file:uploadFiles) {
        imageFiles.add(file);
      }
      updateForm.setImageFiles(imageFiles);
    }

    model.addAttribute("form", updateForm);

    return "product/updateForm";
  }

  //수정
  @PostMapping("/{id}/edit")
  public String update(@PathVariable("id") Long productId,
                       @Valid @ModelAttribute("form") UpdateForm updateForm,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) {

    if (bindingResult.hasErrors()) {
      log.info("bindingResult={}", bindingResult);
      return "product/updateForm";
    }

    //필드검증
    //상품수량은 100초과 금지
    if(updateForm.getQuantity() > 100){
      bindingResult.rejectValue("quantity","product.quantity",new Integer[]{100},"상품수량 초과");
      log.info("bindingResult={}", bindingResult);
      return "product/saveForm";
    }

    //오브젝트검증
    //총액(상품수량*단가) 1000만원 초과금지
    if(updateForm.getQuantity() * updateForm.getPrice() > 10_000_000L){
      bindingResult.reject("product.totalPrice",new Integer[]{1000},"총액 초과!");
      log.info("bindingResult={}", bindingResult);
      return "product/saveForm";
    }

    Product product = new Product();
    BeanUtils.copyProperties(updateForm, product);
    productSVC.update(productId, product);

    redirectAttributes.addAttribute("id", productId);
    return "redirect:/products/{id}/detail";
  }

  //삭제
  @GetMapping("/{id}/del")
  public String deleteById(@PathVariable("id") Long productId) {

    productSVC.deleteByProductId(productId);

    return "redirect:/products/all";  //항시 절대경로로
  }

  //목록
  @GetMapping
  public String findAll(Model model) {
    List<Product> products = productSVC.findAll();

    List<Product> list = new ArrayList<>();
    products.stream().forEach(product->{
      BeanUtils.copyProperties(product,new DetailForm());
      list.add(product);
    });
    model.addAttribute("list", list);
    return "product/all";
  }



}