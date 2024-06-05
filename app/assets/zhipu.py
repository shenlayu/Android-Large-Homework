# from zhipuai import ZhipuAI
# client = ZhipuAI(api_key="68d6ffb374a1b5bb536106376b48c6f0.lrG2n2EfXsdAKU9k") # 填写您自己的APIKey
# response = client.chat.completions.create(
#     model="glm-4",  # 填写需要调用的模型名称
#     messages=[
#         {"role": "user", "content": "作为一名营销专家，请为我的产品创作一个吸引人的slogan"},
#         {"role": "assistant", "content": "当然，为了创作一个吸引人的slogan，请告诉我一些关于您产品的信息"},
#         {"role": "user", "content": "智谱AI开放平台"},
#         {"role": "assistant", "content": "智启未来，谱绘无限一智谱AI，让创新触手可及!"},
#         {"role": "user", "content": "创造一个更精准、吸引人的slogan"},
#         {"role": "assistant", "content": "不行"},
#         {"role": "user", "content": "为什么不行"}
#     ],
# )
# print(response.choices[0].message.content)

import time
from zhipuai import ZhipuAI
import os
import subprocess

if __name__ == "__main__":
    subprocess.call('conda activate android', shell=True)
    client = ZhipuAI(api_key="68d6ffb374a1b5bb536106376b48c6f0.lrG2n2EfXsdAKU9k") # 请填写您自己的APIKey
    content = os.getenv("SCRIPT_CONTENT")
    
    response = client.chat.asyncCompletions.create(
        model="glm-4",  # 填写需要调用的模型名称
        messages=[
            {
                "role": "user",
                "content": "我接下来将给你发送一段笔记，请你帮我对其内容进行概括。"
            },
            {
                "role": "assistant",
                "content": "好的，我将为您概括您的笔记内容。请给我您的笔记。"
            },
            {
                "role": "user",
                "content": content
            }
        ],
    )
    # 获取响应ID
    task_id = response.id
    task_status = ''
    get_cnt = 0

    while task_status != 'SUCCESS' and task_status != 'FAILED' and get_cnt <= 40:
        # 查询响应结果
        result_response = client.chat.asyncCompletions.retrieve_completion_result(id=task_id)

        task_status = result_response.task_status
        if task_status == 'SUCCESS':
            print(result_response.choices[0].message.content)

        time.sleep(2)
        get_cnt += 1