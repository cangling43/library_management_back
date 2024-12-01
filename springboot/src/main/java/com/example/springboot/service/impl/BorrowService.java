package com.example.springboot.service.impl;

import com.example.springboot.controller.request.BaseRequest;
import com.example.springboot.entity.Book;
import com.example.springboot.entity.Borrow;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.BookMapper;
import com.example.springboot.mapper.BorrowMapper;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.IBorrowService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class BorrowService implements IBorrowService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    BookMapper bookMapper;
    @Resource
    BorrowMapper borrowMapper;

    @Override
    public List<Borrow> list() {
        return borrowMapper.list();
    }

    @Override
    public PageInfo<Borrow> page(BaseRequest baseRequest) {
        PageHelper.startPage(baseRequest.getPageNum(),baseRequest.getPageSize());
        return new PageInfo<>(borrowMapper.listBycondition(baseRequest));
    }

    @Override
    @Transactional
    public void save(Borrow obj) {
//        1. 校验积分是否足够
        String userNo = obj.getUserNo();
        User user = userMapper.getByName(userNo);
        if(Objects.isNull(user)){
            throw new  ServiceException("用户不存在!");
        }
//        2. 校验图书数量是否足够
        Book book = bookMapper.getByNo(obj.getBookNo());
        if(Objects.isNull(book)){
            throw new  ServiceException("所借图书不存在!");
        }
        Integer account = user.getAccount();
        Integer score=book.getScore();
//        3.校验账户余额
        if(score>account){
            throw new  ServiceException("积分不足!");
        }
//        更新余额
        user.setAccount(user.getAccount()-score);
        userMapper.updateById(user);

//        更新图书数量
        if(book.getNums()<1){
            throw new  ServiceException("图书数量不足!");
        }
        //        更新图书
        book.setNums(book.getNums()-1);
        bookMapper.updateById(book);

        borrowMapper.save(obj);
    }

    @Override
    public Borrow getById(Integer id) {
        return borrowMapper.getById(id);
    }

    @Override
    public void update(Borrow obj) {
        obj.setUpdatetime(LocalDate.now());
        borrowMapper.updateById(obj);
    }

    @Override
    public void deleteById(Integer id) {
        borrowMapper.deleteById(id);
    }
}
