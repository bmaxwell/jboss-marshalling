/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.test.marshalling;

import org.jboss.marshalling.Unmarshaller;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.marshalling.ByteInput;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.ObjectInputStreamUnmarshaller;
import org.jboss.marshalling.ObjectResolver;
import org.jboss.marshalling.ClassResolver;
import org.jboss.marshalling.SimpleClassResolver;
import org.testng.SkipException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.InputStream;
import java.io.ObjectStreamClass;

/**
 *
 */
public final class ObjectInputStreamTestUnmarshallerProvider implements TestUnmarshallerProvider {

    public Unmarshaller create(final MarshallingConfiguration config, final ByteInput source) throws IOException {
        final MyObjectInputStream ois = new MyObjectInputStream(config, Marshalling.createInputStream(source));
        return ois.unmarshaller;
    }

    private static final class MyObjectInputStream extends ObjectInputStream {
        private final ObjectResolver objectResolver;
        private final ClassResolver classResolver;
        private final Unmarshaller unmarshaller;

        private MyObjectInputStream(final MarshallingConfiguration config, final InputStream in) throws IOException {
            super(in);
            if (config.getClassTable() != null) {
                throw new SkipException("class tables not supported");
            }
            if (config.getObjectTable() != null) {
                throw new SkipException("object tables not supported");
            }
            final ObjectResolver objectResolver = config.getObjectResolver();
            this.objectResolver = objectResolver == null ? Marshalling.nullObjectResolver() : objectResolver;
            final ClassResolver classResolver = config.getClassResolver();
            this.classResolver = classResolver == null ? new SimpleClassResolver(getClass().getClassLoader()) : classResolver;
            enableResolveObject(true);
            //noinspection ThisEscapedInObjectConstruction
            unmarshaller = new ObjectInputStreamUnmarshaller(this);
        }

        protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            return classResolver.resolveClass(unmarshaller, desc.getName(), desc.getSerialVersionUID());
        }

        protected Class<?> resolveProxyClass(final String[] interfaces) throws IOException, ClassNotFoundException {
            return classResolver.resolveProxyClass(unmarshaller, interfaces);
        }

        protected Object resolveObject(final Object obj) throws IOException {
            return objectResolver.readResolve(obj);
        }
    }
}
