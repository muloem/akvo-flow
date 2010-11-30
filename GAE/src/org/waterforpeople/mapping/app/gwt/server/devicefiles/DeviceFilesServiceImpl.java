package org.waterforpeople.mapping.app.gwt.server.devicefiles;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;

import java.util.ArrayList;
import java.util.List;

import org.datanucleus.store.appengine.query.JDOCursorHelper;
import org.waterforpeople.mapping.app.gwt.client.devicefiles.DeviceFilesDto;
import org.waterforpeople.mapping.app.gwt.client.devicefiles.DeviceFilesService;
import org.waterforpeople.mapping.app.util.DtoMarshaller;
import org.waterforpeople.mapping.dao.DeviceFilesDao;
import org.waterforpeople.mapping.domain.Status;

import com.gallatinsystems.device.domain.DeviceFiles;
import com.gallatinsystems.framework.gwt.dto.client.ResponseDto;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DeviceFilesServiceImpl extends RemoteServiceServlet implements
		DeviceFilesService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5012633874057293188L;

	@Override
	public String reprocessDeviceFile(Long id) {
		String reprocessingMessage = null;
		Queue queue = QueueFactory.getDefaultQueue();
		DeviceFilesDao dfDao = new DeviceFilesDao();
		DeviceFiles df = dfDao.getByKey(id);
		if (df != null) {
			reprocessingMessage = "kicked off reprocessing";
			df.setProcessedStatus(Status.StatusCode.REPROCESSING);
			String[] fileNameParts = df.getURI().split("/");
			String fileName = fileNameParts[fileNameParts.length - 1];
			queue.add(url("/app_worker/task").param("action", "processFile")
					.param("fileName", fileName)
					.param("phoneNumber", df.getPhoneNumber())
					.param("checksum", df.getChecksum()));
			dfDao.save(df);
		}
		// log.info("submiting task for fileName: " + fileName);
		return reprocessingMessage;
	}

	@Override
	public ResponseDto<ArrayList<DeviceFilesDto>> listDeviceFiles(
			String processingStatus, String cursor) {
		DeviceFilesDao dfDao = new DeviceFilesDao();
		List<DeviceFiles> dfList = (List<DeviceFiles>) dfDao.list(cursor);
		ArrayList<DeviceFilesDto> dfDtoList = new ArrayList<DeviceFilesDto>();
		for (DeviceFiles canonical : dfList) {
			DeviceFilesDto dto = new DeviceFilesDto();
			DtoMarshaller.copyToDto(canonical, dto);
			dfDtoList.add(dto);
		}
		ResponseDto<ArrayList<DeviceFilesDto>> responseDto = new ResponseDto<ArrayList<DeviceFilesDto>>();
		responseDto.setCursorString(JDOCursorHelper.getCursor(dfList)
				.toWebSafeString());
		responseDto.setPayload(dfDtoList);
		return responseDto;
	}

}
