package ermes.facebook;

import java.util.HashSet;
import java.util.Set;
import org.springframework.context.annotation.Configuration;
import com.restfb.scope.FacebookPermissions;
import ermes.facebook.FacebookService.FacebookServicePermission;

@Configuration
public class FacebookPermission implements FacebookServicePermission {
	public FacebookPermission() {
		permissions=new HashSet<>();
	}
	
	@Override
	public FacebookServicePermission createPermissions() {
		return new FacebookPermission();
	}
	
	@Override
	public void addPermission(ServicePermission permission) {
		permissions.add(permission);
	}
	
	@Override
	public void addPermission(String permission, String status) {
		permissions.add(new Permission(FacebookPermissions.valueOf(permission), status));
	}
	
	@Override
	public void addPermission(String permission) {
		permissions.add(new Permission(FacebookPermissions.valueOf(permission), ServicePermission.WAITING));
	}
	
	@Override
	public void addPermission(FacebookPermissions permission) {
		permissions.add(new Permission(permission, ServicePermission.WAITING));
	}
	
	@Override
	public boolean grantedPermissions(FacebookServicePermission permissions) {
		if(permissions==null)
			return false;
		
		for(ServicePermission perm: permissions.getPermissions())
			if(!isGranted(perm) || !contains(perm))
				return false;
		
		return true;
	}
	
	@Override
	public boolean isGranted(ServicePermission permission) {		
		if(permission==null)
			return false;
		
		for(ServicePermission perm: permissions) {
			if(perm.getPermission().getPermissionString().equalsIgnoreCase(permission.getPermission().getPermissionString())) {
				if(perm.getStatus().equalsIgnoreCase(ServicePermission.DECLINED))
					return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean contains(ServicePermission permission) {
		if(permission==null) 
			return false;
		
		for(ServicePermission perm: permissions) {
			if(perm.getPermission().getPermissionString().equalsIgnoreCase(permission.getPermission().getPermissionString())) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder toStringBuilder=new StringBuilder();
		
		for(ServicePermission permission: permissions) {
			toStringBuilder.append("\t[Permission: ")
				.append(permission.getPermission().getPermissionString())
				.append(", Status: ")
				.append(permission.getStatus())
				.append("]\n");
		}
		
		return getClass().getName() + " [Permissions:\n" + toStringBuilder.toString() + "]";
	}

	@Override
	public Set<ServicePermission> getPermissions() {
		return permissions;
	}

	@Override
	public void setPermissions(Set<ServicePermission> permissions) {
		this.permissions=permissions;
	}
	
	//Inner class
	private class Permission implements ServicePermission {
		public Permission(FacebookPermissions permission, String status) {
			this.permission=permission;
			this.status=status;
		}
		
		@Override
		public FacebookPermissions getPermission() {
			return permission;
		}
		
		@Override
		public void setPermission(FacebookPermissions permission) {
			this.permission=permission;
		}
		
		@Override
		public String getStatus() {
			return status;
		}
		
		@Override
		public void setStatus(String status) {
			this.status=status;
		}
		
		private FacebookPermissions permission;
		private String status;
	}

	private Set<ServicePermission> permissions;
}
