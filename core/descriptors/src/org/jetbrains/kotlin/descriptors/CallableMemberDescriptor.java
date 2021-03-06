/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.descriptors;

import jet.runtime.typeinfo.KotlinSignature;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface CallableMemberDescriptor extends CallableDescriptor, MemberDescriptor {
    @KotlinSignature("fun getOverriddenDescriptors(): MutableSet<out CallableMemberDescriptor>")
    @NotNull
    @Override
    Set<? extends CallableMemberDescriptor> getOverriddenDescriptors();

    @NotNull
    @Override
    CallableMemberDescriptor getOriginal();

    void addOverriddenDescriptor(@NotNull CallableMemberDescriptor overridden);

    enum Kind {
        DECLARATION,
        FAKE_OVERRIDE,
        DELEGATION,
        SYNTHESIZED
        ;
        
        public boolean isReal() {
            return this != FAKE_OVERRIDE;
        }
    }

    /**
     * Is this a real function or function projection.
     */
    @NotNull
    Kind getKind();

    @NotNull
    CallableMemberDescriptor copy(DeclarationDescriptor newOwner, Modality modality, Visibility visibility, Kind kind, boolean copyOverrides);
}
