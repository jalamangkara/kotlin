/*
 * Copyright 2010-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

#include "ReferenceOps.hpp"
#include "ThreadData.hpp"
#include "ThreadRegistry.hpp"

using namespace kotlin;

template<bool kOnStack>
void mm::RefAccessor<kOnStack>::afterStore(ObjHeader* value) noexcept {
    if (kOnStack) {
        afterStackRefStore(direct_, value);
    } else {
        afterHeapRefStore(direct_, value);
    }
}
template void mm::RefAccessor<true>::afterStore(ObjHeader*) noexcept;
template void mm::RefAccessor<false>::afterStore(ObjHeader*) noexcept;

template <bool kOnStack>
void mm::RefAccessor<kOnStack>::beforeStore(ObjHeader* value) noexcept {
    if (kOnStack) {
        beforeStackRefStore(direct_, value);
    } else {
        beforeHeapRefStore(direct_, value);
    }
}
template void mm::RefAccessor<true>::beforeStore(ObjHeader*) noexcept;
template void mm::RefAccessor<false>::beforeStore(ObjHeader*) noexcept;

template <bool kOnStack>
void mm::RefAccessor<kOnStack>::afterLoad() noexcept {
    if (kOnStack) {
        afterStackRefLoad(direct_);
    } else {
        afterHeapRefLoad(direct_);
    }
}
template void mm::RefAccessor<true>::afterLoad() noexcept;
template void mm::RefAccessor<false>::afterLoad() noexcept;

template <bool kOnStack>
void mm::RefAccessor<kOnStack>::beforeLoad() noexcept {
    if (kOnStack) {
        beforeStackRefLoad(direct_);
    } else {
        beforeHeapRefLoad(direct_);
    }
}
template void mm::RefAccessor<true>::beforeLoad() noexcept;
template void mm::RefAccessor<false>::beforeLoad() noexcept;

ALWAYS_INLINE void mm::beforeHeapRefStore(mm::DirectRefAccessor ref, ObjHeader* value) noexcept {}
ALWAYS_INLINE void mm::afterHeapRefStore(mm::DirectRefAccessor ref, ObjHeader* value) noexcept {}
ALWAYS_INLINE void mm::beforeStackRefStore(mm::DirectRefAccessor ref, ObjHeader* value) noexcept {}
ALWAYS_INLINE void mm::afterStackRefStore(mm::DirectRefAccessor ref, ObjHeader* value) noexcept {}
ALWAYS_INLINE void mm::beforeHeapRefLoad(mm::DirectRefAccessor ref) noexcept {}
ALWAYS_INLINE void mm::afterHeapRefLoad(mm::DirectRefAccessor ref) noexcept {}
ALWAYS_INLINE void mm::beforeStackRefLoad(mm::DirectRefAccessor ref) noexcept {}
ALWAYS_INLINE void mm::afterStackRefLoad(mm::DirectRefAccessor ref) noexcept {}

ALWAYS_INLINE OBJ_GETTER(mm::weakRefReadBarrier, std::atomic<ObjHeader*>& referee) noexcept {
    RETURN_RESULT_OF(kotlin::gc::tryRef, referee);
}
