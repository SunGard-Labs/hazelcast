/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.map.operation;

import com.hazelcast.map.MapDataSerializerHook;
import com.hazelcast.map.MapService;
import com.hazelcast.map.record.Record;
import com.hazelcast.map.RecordStore;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spi.BackupOperation;

public final class RemoveBackupOperation extends KeyBasedMapOperation implements BackupOperation, IdentifiedDataSerializable {

    private boolean unlockKey = false;

    public RemoveBackupOperation(String name, Data dataKey) {
        super(name, dataKey);
    }

    public RemoveBackupOperation(String name, Data dataKey, boolean unlockKey) {
        super(name, dataKey);
        this.unlockKey = unlockKey;
    }

    public RemoveBackupOperation() {
    }

    public void run() {
        MapService mapService = (MapService) getService();
        int partitionId = getPartitionId();
        RecordStore recordStore = mapService.getRecordStore(partitionId, name);
        Record record = recordStore.getRecords().get( dataKey );
        if (record != null) {
            recordStore.getRecords().remove( dataKey );
            updateSizeEstimator( -calculateRecordSize(record) );
        }
        if(unlockKey)
            recordStore.forceUnlock(dataKey);
    }

    @Override
    public Object getResponse() {
        return Boolean.TRUE;
    }

    public int getFactoryId() {
        return MapDataSerializerHook.F_ID;
    }

    public int getId() {
        return MapDataSerializerHook.REMOVE_BACKUP;
    }

    private void updateSizeEstimator( long recordSize ) {
        mapService.getMapContainer( name ).getSizeEstimator().add( recordSize );
    }

    private long calculateRecordSize( Record record ) {
        return mapService.getMapContainer( name ).getSizeEstimator().getCost( record );
    }

}
