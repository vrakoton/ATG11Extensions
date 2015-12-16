/**
 * @author vrakoton
 * @version 1.0
 * 
 * Description : 
 * 
 * This is a bean reprensenting the cache statistics for a given item descriptor
 * 
 */
package atg.adapter.gsa;

import atg.adapter.gsa.GSAItemDescriptor;

public class ItemDescriptorStatistics {
  GSAItemDescriptor mItemDescriptor;
  ItemCache mItemCache;
  
  /**
   * Constructor
   * @param pItemDesc
   */
  public ItemDescriptorStatistics (GSAItemDescriptor pItemDesc) {
    mItemDescriptor = pItemDesc;
    mItemCache = pItemDesc.getItemCache();
  }
  
  public void resetStatistics () {
    if (mItemCache != null)
      mItemCache.resetStats();
  }
  
  public String getItemDescriptorName () {
    return mItemDescriptor.getItemDescriptorName();
  }
  
  public int getEntryCount () {
    return mItemCache.getEntryCount();
  }
  
  public int getWeakEntryCount () {
    return mItemCache.getWeakEntryCount();
  }
  
  public int getItemCacheSize () {
    return mItemCache.getItemCacheSize();
  }
  
  public int getAccessCount () {
    return mItemCache.getAccessCount();
  }
  
  public int getHitCount() {
    return mItemCache.getHitCount();
  }
  
  public int getMissCount() {
    return mItemCache.getMissCount();
  }
  
  public double getUsedRatio () {
    return calculatePercentage(getEntryCount(), getItemCacheSize());
  }
  
  public double getHitRatio () {
    return calculatePercentage(getHitCount(), getAccessCount());
  }
  
  /**
   * Calculates a percentage
   * @param pNumerator
   * @param pDenominator
   * @return
   */
  double calculatePercentage(int pNumerator, int pDenominator)
  {
    if (pDenominator == 0.0D) return 0.0D;
    double val = pNumerator / pDenominator;
    val = (int)(val * 10000.0D + 0.5D) / 100.0D;
    return val;
  }
  
  /**
   * Formatting the class data for 
   */
  public String toString () {
    StringBuffer buffer = new StringBuffer(getItemDescriptorName());
    buffer.append("{Item cache size:Used ratio:Entry count:AccessCount:Hit count:Miss count:Hit ratio},");
    buffer.append(getItemCacheSize());
    buffer.append(",");
    buffer.append(getUsedRatio());
    buffer.append(",");
    buffer.append(getEntryCount());
    buffer.append(",");
    buffer.append(getAccessCount());
    buffer.append(",");
    buffer.append(getHitCount());
    buffer.append(",");
    buffer.append(getMissCount());
    buffer.append(",");
    buffer.append(getHitRatio());
    return buffer.toString();
  }
}
