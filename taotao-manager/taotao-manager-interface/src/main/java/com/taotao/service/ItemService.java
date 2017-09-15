package com.taotao.service;

import com.taotao.common.pojo.EasyUIDataGridResult;
import com.taotao.common.pojo.TaotaoResult;
import com.taotao.pojo.TbItem;
import com.taotao.pojo.TbItemDesc;

public interface ItemService {
	public TbItem getItemById(long id);
	EasyUIDataGridResult getItemList(int page,int rows);
	TaotaoResult addItem(TbItem item, String desc);
	TbItemDesc getItemDescById(long itemId);
}
