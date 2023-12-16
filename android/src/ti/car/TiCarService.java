package ti.car;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.car.app.CarAppService;
import androidx.car.app.Screen;
import androidx.car.app.Session;
import androidx.car.app.validation.HostValidator;

import org.appcelerator.kroll.common.Log;

public class TiCarService extends CarAppService
{
	@NonNull
	@Override
	public HostValidator createHostValidator()
	{
		return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
	}

	@NonNull
	@Override
	public Session onCreateSession()
	{
		return new Session()
		{
			@NonNull
			@Override
			public Screen onCreateScreen(@NonNull Intent intent)
			{
				return new TiCarScreen(getCarContext());
			}
		};
	}
}
