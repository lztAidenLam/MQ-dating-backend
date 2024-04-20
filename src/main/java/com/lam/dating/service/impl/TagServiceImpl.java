package com.lam.dating.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lam.dating.model.entity.Tag;
import com.lam.dating.service.TagService;
import com.lam.dating.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author AidenLam
* @description 针对表【dt_tag(标签)】的数据库操作Service实现
* @createDate 2024-04-17 23:28:54
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




