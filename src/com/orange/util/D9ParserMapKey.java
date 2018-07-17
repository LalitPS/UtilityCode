package com.orange.util;

public class D9ParserMapKey {

	private final String coreProduct, href;
	
	public D9ParserMapKey(String coreProduct, String href){
		this.coreProduct = coreProduct.toUpperCase().trim();
		this.href= href.toUpperCase().trim();
	}
	
	
	 @Override
	public boolean equals(Object obj) {
	    return (obj instanceof D9ParserMapKey) && ((D9ParserMapKey) obj).coreProduct.equals(coreProduct)
	                                   && ((D9ParserMapKey) obj).href.equals(href);
	}


	public String getCoreProduct() {
		return coreProduct;
	}


	public String getHref() {
		return href;
	}

	    @Override
		    public int hashCode() {
		        return coreProduct.hashCode() ^ href.hashCode();
		    }
	    
}
