package com.kuney.community.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kuneychen
 * @since 2022/6/10 14:47
 */
public class PageUtils {
    public static <T> Page<T> handle(long pageNumber, long pageSize, long total, List<T> records) {
        List<T> list;
        if (ObjCheckUtils.isEmpty(records)) {
            total = 0;
            list = new ArrayList<>();
        } else {
            list = records;
        }
        return new Page<T>().setCurrent(pageNumber)
                .setTotal(total)
                .setSize(pageSize)
                .setRecords(list);
    }

    /**
     *
     * @param pages 总页数
     * @param pageNum 当前页
     * @return 页码范围[begin, end]
     */
    public static long[] getPageRange(long pages, long pageNum) {
        long pageEnd = Math.min(pages, Math.max(5, pageNum + 1));
        long pageBegin = Math.max(Constants.PAGE_NUM, pageEnd - 4);
        return new long[]{pageBegin, pageEnd};
    }
}
