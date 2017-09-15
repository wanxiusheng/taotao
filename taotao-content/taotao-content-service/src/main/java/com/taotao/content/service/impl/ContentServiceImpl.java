package com.taotao.content.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mysql.fabric.xmlrpc.base.Array;
import com.taotao.common.pojo.EasyUIDataGridResult;
import com.taotao.common.pojo.TaotaoResult;
import com.taotao.common.utils.JsonUtils;
import com.taotao.content.service.ContentService;
import com.taotao.jedis.JedisClient;
import com.taotao.mapper.TbContentMapper;
import com.taotao.pojo.TbContent;
import com.taotao.pojo.TbContentExample;
import com.taotao.pojo.TbContentExample.Criteria;
import com.taotao.pojo.TbItem;
import com.taotao.pojo.TbItemExample;

@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	
	@Autowired
	private JedisClient jedisClient;
	
	@Value("${INDEX_CONTENT}")
	private String INDEX_CONTENT;
	
	@Override
	public TaotaoResult addContent(TbContent content) {
		//补全pojo的属性
		content.setCreated( new Date());
		content.setUpdated(new Date());
		//插入到内容表
		contentMapper.insert(content);
		//删除对应的缓存信息
		jedisClient.hdel(INDEX_CONTENT, content.getCategoryId().toString());
		return TaotaoResult.ok();
	}

	@Override
	public EasyUIDataGridResult getContent(long categoryId, Integer page, Integer rows) {
		// 根据Id查内容
		//设置分页信息
		PageHelper.startPage(page, rows);
		//执行查询
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		criteria.andCategoryIdEqualTo(categoryId);
		List<TbContent> list = contentMapper.selectByExample(example);
		PageInfo<TbContent> pageInfo = new PageInfo<>(list);
		EasyUIDataGridResult result = new EasyUIDataGridResult();
		result.setRows(list);
		result.setTotal(pageInfo.getTotal());
		return result;
	}

	@Override
	public TaotaoResult updateContent(TbContent content) {
		// TODO Auto-generated method stub
		content.setUpdated(new Date());
		contentMapper.updateByPrimaryKeySelective(content);
		return TaotaoResult.ok();
	}

	@Override
	public TaotaoResult deleteContent(long[] ints) {
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		List<Long> list=new ArrayList<>();
		for(int j=0;j<ints.length;j++){
			list.add(ints[j]);
		}
		criteria.andIdIn(list);
		contentMapper.deleteByExample(example);
		return TaotaoResult.ok();
	}

	@Override
	public List<TbContent> getContentByCid(long cid) {
		
		//先查询缓存
				//添加缓存不能影响正常业务逻辑
				try {
					//查询缓存
					String json = jedisClient.hget(INDEX_CONTENT, cid + "");
					//查询到结果，把json转换成List返回
					if (StringUtils.isNotBlank(json)) {
						List<TbContent> list = JsonUtils.jsonToList(json, TbContent.class);
						return list;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		
		
		//缓存中没有命中，需要查询数据库
				TbContentExample example = new TbContentExample();
				Criteria criteria = example.createCriteria();
				//设置查询条件
				criteria.andCategoryIdEqualTo(cid);
				//执行查询
				List<TbContent> list = contentMapper.selectByExample(example);
				
				//把结果添加到缓存
				try {
					jedisClient.hset(INDEX_CONTENT, cid + "", JsonUtils.objectToJson(list));
				} catch (Exception e) {
					e.printStackTrace();
				}
				//返回结果
				return list;
	}

}
