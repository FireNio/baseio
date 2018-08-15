/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.baseio.buffer;

final class PooledHeapByteBuf extends AbstractHeapByteBuf implements PooledByteBuf {

    protected PooledHeapByteBuf(ByteBufAllocator allocator, byte[] memory) {
        super(allocator, memory);
    }

    private int beginUnit;

    @Override
    public int getBeginUnit() {
        return beginUnit;
    }

    @Override
    public PooledByteBuf newByteBuf(ByteBufAllocator allocator) {
        return this;
    }

    @Override
    public ByteBuf duplicate() {
        if (isReleased()) {
            throw new ReleasedException("released");
        }
        addReferenceCount();
//        return new DuplicatedByteBuf(new PooledHeapByteBuf(allocator, memory).produce(this), this);
        return new DuplicatedHeapByteBuf(memory, this).produce(this);
    }

    @Override
    public PooledHeapByteBuf produce(int begin, int end, int newLimit) {
        this.offset = begin * allocator.getUnitMemorySize();
        this.capacity = (end - begin) * allocator.getUnitMemorySize();
        this.position = offset;
        this.limit = offset + newLimit;
        this.beginUnit = begin;
        this.referenceCount = 1;
        return this;
    }

    @Override
    public PooledByteBuf produce(PooledByteBuf buf) {
        this.offset = buf.offset();
        this.capacity = buf.capacity();
        this.position = offset + buf.position();
        this.limit = offset + buf.limit();
        this.beginUnit = buf.getBeginUnit();
        this.referenceCount = 1;
        return this;
    }

}
