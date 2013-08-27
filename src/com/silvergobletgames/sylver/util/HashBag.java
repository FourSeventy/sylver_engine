
package com.silvergobletgames.sylver.util;

import com.silvergobletgames.sylver.core.SceneObject;
import java.util.HashMap;
import java.util.HashSet;


public class HashBag<K,V> 
{
     //bag map
     private HashMap<K, HashSet<V>> bagMap = new HashMap();
     
     /**
      * Add an entry into the bag
      * @param key key of the entry
      * @param value value to add into the bag
      */
     public void put(K key, V value)
     {
         //see if the bag exists
         if(bagMap.get(key) == null)
         { 
             //if not add a new entry and hashset for it
             bagMap.put(key, new HashSet<V>());
         }
         
         //add the entry to the bag
         bagMap.get(key).add(value);
     }
     
     /**
      * Get the set that corresponds to the key
      * @param key key to get the set of
      * @return HashSet that corresponds to this key
      */
     public HashSet<V> get(K key)
     {
         //see if the bag exists
         if(bagMap.get(key) == null)
         { 
             //if not add a new entry and hashset for it
             bagMap.put(key, new HashSet<V>());
         }
         
         //return the bag
         return bagMap.get(key);
     }
    
}
