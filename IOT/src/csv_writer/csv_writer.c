#include"csv_writer.h"
#include"header.h"
int write_csv(char* value,int number,int count)
{
	char file_name[150];
	int fd;
	sprintf(file_name,"%s%s%d_%d",FOLDER_NAME,FILE_NAME,number,count);
	fd = open(file_name,O_WRONLY|O_CREAT|O_APPEND,0777);
	write(fd,value,strlen(value));
	close(fd);
	return 1;
}
int write_csv_to_fd(int fd, char* value,int len)
{
	write(fd,value,len);
	return 1;
}
int group_csv_file(int number)
{
	int fd[7],sfd=-1,i,now_count,r_len;
	char file_name[7][150];
	char object[1024];
	for(i = 0 ; i <7;i++)
	{
		sprintf(file_name[i],"%s%s%d_%d",FOLDER_NAME,FILE_NAME,i+1,number);
		fd[i] = open(file_name[i],O_RDONLY,0777);
		if(fd[i] <0)
		{
			now_count = i;
			break;
		}
	}
	while(sfd<0)
	{
		sfd = open(SEND_FILE_NAME,O_CREAT|O_EXCL|O_WRONLY,0777);
		usleep(500);
	}
	for(i=0;i<now_count;i++)
	{
		if(fd[i] != 0)
		{
			while((r_len= read(fd[i],object,1024))>0)
			{
				send_msg msg;
				msg.value = object;
				msg.len = r_len;
				write_csv_to_fd(sfd,msg.value,msg.len);
			}
			remove(file_name[i]);
		}
	}
}