package com.ginkgocap.ywxt.interlocution.id;

public class DefaultIdGeneratorConfig implements IdGeneratorConfig
{
	  private final String prefix;
	  public DefaultIdGeneratorConfig(String prefix)
	  {
		  this.prefix = prefix;
	  }
	  @Override
	  public String getSplitString() {
	    return "";
	  }

	  @Override
	  public int getInitial() {
	    return 1;
	  }

	  @Override
	  public String getPrefix() {
	    return prefix;
	  }

	  @Override
	  public int getRollingInterval() {
	    return 1;
	  }
	}
