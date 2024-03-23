import { GithubOutlined } from '@ant-design/icons';
import { DefaultFooter } from '@ant-design/pro-components';
import '@umijs/max';
import React from 'react';

const Footer: React.FC = () => {
  const defaultMessage = '司雨枫';
  const currentYear = new Date().getFullYear();
  return (
    <DefaultFooter
      style={{
        background: 'none',
      }}
      copyright={`${currentYear} ${defaultMessage}`}
      links={[
        {
          key: 'Ant Design',
          title: '飒萨',
          href: 'https://github.com/siyufeng1',
          blankTarget: true,
        },
        {
          key: 'github',
          title: (
            <>
              <GithubOutlined /> 代码生成器
            </>
          ),
          href: 'https://github.com/siyufeng1/sasa-generator',
          blankTarget: true,
        },
      ]}
    />
  );
};
export default Footer;
