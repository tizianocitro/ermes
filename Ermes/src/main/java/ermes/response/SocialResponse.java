package ermes.response;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description="Modella la risposta per i servizi di integrazione con Social Networks.")
public class SocialResponse<T> implements Serializable {
	private static final long serialVersionUID=3550270890579239665L;
	
	public SocialResponse() {
		clear();
	}

	public void clear() {
		this.success=false;
		this.code=DEFAULT_CODE;
		this.data=null;
	}
	
	public SocialResponse<T> success(String code, String message) {
		this.success=true;
		this.code=code;
		this.message=message;
		
		return this;
	}
	
	public SocialResponse<T> error(String code, String message) {
		this.success=false;
		this.code=code;
		this.message=message;
		this.data=null;
		
		return this;
	}
	@Override
	public String toString() {
		return getClass().getName() + " [code=" + code + ", success=" + success + ", message=" + message + ", data=" + data + "]";
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code=code;
	}

	public boolean isSuccess() {
		return success;
	}
	
	public void setSuccess(boolean success) {
		this.success=success;
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message=message;
	}
	
	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data=data;
	}
	
	@ApiModelProperty(notes="Il codice associato alla risposta.", position=1)
	private String code;
	
	@ApiModelProperty(notes="Indica il successo dell'operazione.", position=2)
	private boolean success;
	
	@ApiModelProperty(notes="Il messaggio associato alla risposta.", position=3)
	private String message;
	
	@ApiModelProperty(notes="Eventuali dati associati alla risposta.", position=4)
	private T data;
	
	public static final String DEFAULT_CODE="0";
	public static final String CODE="200";
	
	public static final boolean SUCCESS=true;
	public static final boolean FAIL=false;
	
	public static final String SUCCES_MESSAGE="Operazione effettuata con successo";
	public static final String FAIL_MESSAGE="Si è verificato un errore durante l'operazione";
}
