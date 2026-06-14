package org.Kroj.Core.Tools.ObjectManagement;


import org.Kroj.Core.Tools.ObjectManagement.ObjectManager.ObjectPool;

public class ObjectsPool {

    public final static ObjectPool<StringBuilder> stringBuilders = new ObjectPool<>(StringBuilder::new,10);

}
