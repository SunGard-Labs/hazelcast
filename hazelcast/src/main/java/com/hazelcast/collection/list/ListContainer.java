/*
 * Copyright (c) 2008-2012, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.collection.list;

import com.hazelcast.collection.CollectionContainer;
import com.hazelcast.collection.CollectionItem;
import com.hazelcast.collection.CollectionService;
import com.hazelcast.config.ListConfig;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.spi.NodeEngine;

import java.util.*;

/**
 * @ali 8/30/13
 */
public class ListContainer extends CollectionContainer {

    private List<CollectionItem> itemList = null;
    private ListConfig config;

    public ListContainer() {
    }

    public ListContainer(String name, NodeEngine nodeEngine, CollectionService service) {
        super(name, nodeEngine, service);
    }

    public ListConfig getConfig() {
        if (config == null){
            config = new ListConfig(nodeEngine.getConfig().getListConfig(name));
        }
        return config;
    }

    protected CollectionItem add(int index, Data value){
        final CollectionItem item = new CollectionItem(this, nextId(), value);
        if (index < 0){
            return getCollection().add(item) ? item : null;
        } else {
            getCollection().add(index, item);
            return item;
        }
    }

    protected CollectionItem get(int index){
        return getCollection().get(index);
    }

    protected CollectionItem set(int index, long itemId, Data value){
        return getCollection().set(index, new CollectionItem(this, itemId, value));
    }

    protected void setBackup(long oldItemId, long itemId, Data value){
        getMap().remove(oldItemId);
        getMap().put(itemId, new CollectionItem(this, itemId, value));

    }

    protected CollectionItem remove(int index){
        return getCollection().remove(index);
    }

    protected int indexOf(boolean last, Data value){
        final List<CollectionItem> list = getCollection();
        if (last){
            int index = list.size();
            final ListIterator<CollectionItem> iterator = list.listIterator(index);
            while (iterator.hasPrevious()){
                final CollectionItem item = iterator.previous();
                index--;
                if (value.equals(item.getValue())){
                    return index;
                }
            }
        } else {
            int index = -1;
            final Iterator<CollectionItem> iterator = list.iterator();
            while (iterator.hasNext()){
                final CollectionItem item = iterator.next();
                index++;
                if (value.equals(item.getValue())){
                    return index;
                }
            }
        }
        return -1;
    }

    protected Map<Long, Data> addAll(int index, List<Data> valueList) {
        final int size = valueList.size();
        final Map<Long, Data> map = new HashMap<Long, Data>(size);
        List<CollectionItem> list = new ArrayList<CollectionItem>(size);
        for (Data value : valueList) {
            final long itemId = nextId();
            list.add(new CollectionItem(this, itemId, value));
            map.put(itemId, value);
        }
        getCollection().addAll(index, list);

        return map;
    }

    protected List<Data> sub(int from, int to){
        final List<CollectionItem> list;
        if (from == -1 && to == -1){
            list = getCollection();
        } else {
            list = getCollection().subList(from, to);
        }
        final ArrayList<Data> sub = new ArrayList<Data>(list.size());
        for (CollectionItem item : list) {
            sub.add((Data)item.getValue());
        }
        return sub;
    }

    protected List<CollectionItem> getCollection(){
        if(itemList == null){
            if (itemMap != null && !itemMap.isEmpty()){
                itemList = new ArrayList<CollectionItem>(itemMap.values());
                Collections.sort(itemList);
            } else {
                itemList = new ArrayList<CollectionItem>(1000);
            }
        }
        return itemList;
    }

    protected Map<Long, CollectionItem> getMap(){
        if (itemMap == null){
            if (itemList != null && !itemList.isEmpty()){
                itemMap = new HashMap<Long, CollectionItem>(itemList.size());
                for (CollectionItem item : itemList) {
                    itemMap.put(item.getItemId(), item);
                }
            } else {
                itemMap = new HashMap<Long, CollectionItem>(1000);
            }
        }
        return itemMap;
    }

    protected void onDestroy() {
        if (itemList != null){
            itemList.clear();
        }
        if (itemMap != null){
            itemMap.clear();
        }
    }
}
