package org.kroj.Core.Tools.ObjectManagement;


import org.kroj.Core.Tools.ObjectManagement.ObjectManager.ObjectPool;

public class ObjectsPool {

    public final static ObjectPool<StringBuilder> stringBuilders = new ObjectPool<>(StringBuilder::new,10);

}
